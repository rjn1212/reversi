import { useState } from 'react';
import type { AiDifficulty, GameVariant, Player } from '../../types/game.ts';
import './NewGameDialog.css';

interface NewGameDialogProps {
  onStart: (difficulty: AiDifficulty, variant: GameVariant, side: Player) => void;
  loading: boolean;
}

export default function NewGameDialog({ onStart, loading }: NewGameDialogProps) {
  const [variant, setVariant] = useState<GameVariant>('REVERSIO');
  const [difficulty, setDifficulty] = useState<AiDifficulty>('MEDIUM');
  const [side, setSide] = useState<Player>('BLACK');
  const [showRules, setShowRules] = useState(false);

  return (
    <div className="new-game-dialog">
      <h2>New Game</h2>

      <div className="new-game-dialog__section">
        <label>Game Variant</label>
        <div className="new-game-dialog__options">
          {(['REVERSIO', 'REVERSI'] as GameVariant[]).map(v => (
            <button
              key={v}
              className={`btn btn--option ${variant === v ? 'btn--selected' : ''}`}
              onClick={() => setVariant(v)}
            >
              {v === 'REVERSIO' ? 'Reversio' : 'Reversi'}
            </button>
          ))}
        </div>
        <p className="new-game-dialog__hint">
          {variant === 'REVERSIO'
            ? 'Advanced Reversi: fixed center starting position'
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

      <button
        className="new-game-dialog__rules-toggle"
        onClick={() => setShowRules(!showRules)}
      >
        {showRules ? 'Hide Rules' : 'How to Play'}
      </button>

      {showRules && (
        <div className="new-game-dialog__rules">
          <h3>Objective</h3>
          <p>Have the most discs of your color on the board when the game ends.</p>

          <h3>How to Play</h3>
          <ol>
            <li>Black always moves first.</li>
            <li>Place a disc on an empty square so that it <strong>outflanks</strong> one or more of your opponent's discs — trapping them in a straight line (horizontal, vertical, or diagonal) between your new disc and another disc of your color.</li>
            <li>All outflanked discs are flipped to your color.</li>
            <li>If you have no valid moves, you must pass.</li>
          </ol>

          <h3>Game Over</h3>
          <p>The game ends when the board is full or neither player can move. The player with the most discs wins. A 32–32 split is a draw.</p>

          <h3>Tips</h3>
          <ul>
            <li><strong>Corners</strong> are the strongest positions — discs there can never be flipped.</li>
            <li>Avoid placing next to empty corners — it gives your opponent access to them.</li>
            <li><strong>Fewer discs early</strong> is often better — it limits your opponent's moves.</li>
          </ul>

          {variant === 'REVERSI' && (
            <>
              <h3>Reversi Variant</h3>
              <p>The board starts empty. Each player takes turns placing a disc in one of the four center squares (d4, d5, e4, e5). Once all four are placed, standard outflanking rules begin.</p>
            </>
          )}
        </div>
      )}
    </div>
  );
}
