import { useRef, useState } from 'react';
import { uploadResume } from '../services/resumeService.js';
import { errorMessage } from '../services/api.js';
import { validateResumeFile } from '../utils/validators.js';

export default function ResumeUploader({ onUploaded }) {
  const inputRef = useRef(null);
  const [dragging, setDragging] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const handleFile = async (file) => {
    const problem = validateResumeFile(file);
    if (problem) {
      setError(problem);
      return;
    }
    setError('');
    setBusy(true);
    try {
      const resume = await uploadResume(file);
      onUploaded?.(resume);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
      if (inputRef.current) inputRef.current.value = '';
    }
  };

  return (
    <div>
      <div
        className={`dropzone${dragging ? ' dragging' : ''}`}
        role="button"
        tabIndex={0}
        onClick={() => inputRef.current?.click()}
        onKeyDown={(e) => (e.key === 'Enter' || e.key === ' ') && inputRef.current?.click()}
        onDragOver={(e) => { e.preventDefault(); setDragging(true); }}
        onDragLeave={() => setDragging(false)}
        onDrop={(e) => {
          e.preventDefault();
          setDragging(false);
          handleFile(e.dataTransfer.files?.[0]);
        }}
      >
        <strong>{busy ? 'Uploading…' : 'Drop your resume here or click to choose'}</strong>
        <span>PDF, DOCX or TXT, up to 5 MB</span>
        <input
          ref={inputRef}
          type="file"
          accept=".pdf,.docx,.txt"
          hidden
          onChange={(e) => handleFile(e.target.files?.[0])}
        />
      </div>
      {error && <div className="field-error" role="alert">{error}</div>}
    </div>
  );
}
