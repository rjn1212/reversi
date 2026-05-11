import './GameInfo.css';

interface ScoreBarProps {
  blackCount: number;
  whiteCount: number;
}

export default function ScoreBar({ blackCount, whiteCount }: ScoreBarProps) {
  const total = blackCount + whiteCount || 1;
  const blackPct = (blackCount / total) * 100;

  return (
    <div className="score-bar">
      <div className="score-bar__black" style={{ width: `${blackPct}%` }}>
        {blackCount > 0 && <span>{blackCount}</span>}
      </div>
      <div className="score-bar__white" style={{ width: `${100 - blackPct}%` }}>
        {whiteCount > 0 && <span>{whiteCount}</span>}
      </div>
    </div>
  );
}
