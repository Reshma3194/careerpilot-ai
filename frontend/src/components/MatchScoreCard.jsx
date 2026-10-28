// Semicircle gauge, 0–100. The arc length is the score.
const RADIUS = 80;
const ARC_LENGTH = Math.PI * RADIUS;

export default function MatchScoreCard({ score, summary }) {
  const clamped = Math.max(0, Math.min(100, Number(score) || 0));
  const offset = ARC_LENGTH * (1 - clamped / 100);

  return (
    <div className="score-card">
      <svg
        className="gauge"
        viewBox="0 0 200 120"
        role="img"
        aria-label={`Match score ${clamped} out of 100`}
      >
        <path
          className="gauge-track"
          d="M 20 100 A 80 80 0 0 1 180 100"
          fill="none"
          strokeWidth="14"
          strokeLinecap="round"
        />
        <path
          className="gauge-fill"
          d="M 20 100 A 80 80 0 0 1 180 100"
          fill="none"
          strokeWidth="14"
          strokeLinecap="round"
          strokeDasharray={ARC_LENGTH}
          strokeDashoffset={offset}
        />
        <text className="gauge-value" x="100" y="92" textAnchor="middle">
          {clamped}
        </text>
        <text className="gauge-label" x="100" y="114" textAnchor="middle">
          out of 100
        </text>
      </svg>
      {summary && (
        <div className="score-summary">
          <h3>Overall fit</h3>
          <p>{summary}</p>
        </div>
      )}
    </div>
  );
}
