import { useState } from 'react';
import type { AiDifficulty, GameVariant, Player } from '../../types/game.ts';
import './NewGameDialog.css';

interface NewGameDialogProps {
  onStart: (difficulty: AiDifficulty, variant: GameVariant, side: Player) => void;
  loading: boolean;
}

export default function NewGameDialog({ onStart, loading }: NewGameDialogProps) {
  const [variant, setVariant] = useState<GameVariant>('OTHELLO');
  const [difficulty, setDifficulty] = useState<AiDifficulty>('MEDIUM');
  const [side, setSide] = useState<Player>('BLACK');

  return (
    <div className="new-game-dialog">
      <h2>New Game</h2>

      <div className="new-game-dialog__section">
        <label>Game Variant</label>
        <div className="new-game-dialog__options">
          {(['OTHELLO', 'REVERSI'] as GameVariant[]).map(v => (
            <button
              key={v}
              className={`btn btn--option ${variant === v ? 'btn--selected' : ''}`}
              onClick={() => setVariant(v)}
            >
              {v === 'OTHELLO' ? 'Othello' : 'Reversi'}
            </button>
          ))}
        </div>
        <p className="new-game-dialog__hint">
          {variant === 'OTHELLO'
            ? 'Standard rules: fixed center starting position'
            : 'Classic rules: place first 4 discs freely in center'}
        </p>
      </div>

      <div className="new-game-dialog__section">
        <label>AI Difficulty</label>
        <div className="new-game-dialog__options">
          {(['EASY', 'MEDIUM', 'HARD'] as AiDifficulty[]).map(d => (
            <button
              key={d}
              className={`btn btn--option ${difficulty === d ? 'btn--selected' : ''}`}
              onClick={() => setDifficulty(d)}
            >
              {d.charAt(0) + d.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
      </div>

      <div className="new-game-dialog__section">
        <label>Play As</label>
        <div className="new-game-dialog__options">
          <button
            className={`btn btn--option ${side === 'BLACK' ? 'btn--selected' : ''}`}
            onClick={() => setSide('BLACK')}
          >
            <span className="new-game-dialog__disc new-game-dialog__disc--black" />
            Black (first)
          </button>
          <button
            className={`btn btn--option ${side === 'WHITE' ? 'btn--selected' : ''}`}
            onClick={() => setSide('WHITE')}
          >
            <span className="new-game-dialog__disc new-game-dialog__disc--white" />
            White (second)
          </button>
        </div>
      </div>

      <button
        className="btn btn--primary"
        onClick={() => onStart(difficulty, variant, side)}
        disabled={loading}
      >
        {loading ? 'Starting...' : 'Start Game'}
      </button>
    </div>
  );
}
