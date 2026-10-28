import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { listJobs } from '../services/jobService.js';
import { evaluateAnswer, generateQuestions } from '../services/aiService.js';
import { errorMessage } from '../services/api.js';
import LoadingSpinner from '../components/LoadingSpinner.jsx';

export default function InterviewPrep() {
  const [jobs, setJobs] = useState(null);
  const [jobId, setJobId] = useState('');
  const [questions, setQuestions] = useState([]);
  const [current, setCurrent] = useState(0);
  const [answers, setAnswers] = useState({});
  const [feedback, setFeedback] = useState({});
  const [loadingQuestions, setLoadingQuestions] = useState(false);
  const [evaluating, setEvaluating] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    listJobs()
      .then((j) => {
        setJobs(j);
        if (j.length) setJobId(String(j[0].id));
      })
      .catch((err) => { setJobs([]); setError(errorMessage(err)); });
  }, []);

  const handleGenerate = async () => {
    setError('');
    setLoadingQuestions(true);
    try {
      const result = await generateQuestions(Number(jobId));
      setQuestions(result.questions);
      setCurrent(0);
      setAnswers({});
      setFeedback({});
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoadingQuestions(false);
    }
  };

  const handleEvaluate = async () => {
    const answer = answers[current]?.trim();
    if (!answer) return setError('Write an answer first.');
    setError('');
    setEvaluating(true);
    try {
      const result = await evaluateAnswer({
        question: questions[current].question,
        answer,
        jobId: Number(jobId),
      });
      setFeedback((f) => ({ ...f, [current]: result }));
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setEvaluating(false);
    }
  };

  if (!jobs) return <LoadingSpinner />;

  if (jobs.length === 0) {
    return (
      <>
        <div className="page-head"><h1>Interview prep</h1></div>
        <p className="empty">Interview questions are based on a job. <Link to="/jobs">Add a job</Link> first.</p>
      </>
    );
  }

  const q = questions[current];
  const fb = feedback[current];

  return (
    <>
      <div className="page-head">
        <h1>Interview prep</h1>
        <p>Practice questions for a specific job, then get feedback on your answers.</p>
      </div>

      <section className="panel">
        <div className="row">
          <div style={{ flex: 1, minWidth: 220 }}>
            <label htmlFor="job">Job</label>
            <select id="job" value={jobId} onChange={(e) => setJobId(e.target.value)}>
              {jobs.map((j) => <option key={j.id} value={j.id}>{j.title} · {j.company}</option>)}
            </select>
          </div>
          <button type="button" className="btn" style={{ alignSelf: 'flex-end' }} onClick={handleGenerate} disabled={loadingQuestions}>
            {loadingQuestions ? 'Generating…' : questions.length ? 'New questions' : 'Generate questions'}
          </button>
        </div>
        {loadingQuestions && <LoadingSpinner label="Writing questions. This can take up to a minute." />}
      </section>

      {error && <div className="alert alert-error" role="alert">{error}</div>}

      {q && (
        <div className="split" style={{ gridTemplateColumns: '1fr 1.4fr' }}>
          <section>
            <ul className="question-list">
              {questions.map((item, i) => (
                <li key={i}>
                  <button type="button" aria-current={i === current} onClick={() => { setCurrent(i); setError(''); }}>
                    {item.question}
                    <small>{item.category}{feedback[i] ? ` · scored ${feedback[i].score}/10` : ''}</small>
                  </button>
                </li>
              ))}
            </ul>
          </section>

          <section className="panel">
            <h2>{q.question}</h2>
            <div className="field">
              <label htmlFor="answer">Your answer</label>
              <textarea
                id="answer"
                value={answers[current] ?? ''}
                onChange={(e) => setAnswers((a) => ({ ...a, [current]: e.target.value }))}
              />
            </div>
            <button type="button" className="btn" onClick={handleEvaluate} disabled={evaluating}>
              {evaluating ? 'Evaluating…' : 'Get feedback'}
            </button>

            {fb && (
              <div className="list-block" style={{ marginTop: '1.25rem' }}>
                <h3>Score: {fb.score} out of 10</h3>
                <p>{fb.feedback}</p>
                {fb.strengths?.length > 0 && (<><h3>What worked</h3><ul>{fb.strengths.map((s, i) => <li key={i}>{s}</li>)}</ul></>)}
                {fb.improvements?.length > 0 && (<><h3>How to improve</h3><ul>{fb.improvements.map((s, i) => <li key={i}>{s}</li>)}</ul></>)}
                {fb.sampleAnswer && (<><h3>Example answer</h3><p>{fb.sampleAnswer}</p></>)}
              </div>
            )}
          </section>
        </div>
      )}
    </>
  );
}
