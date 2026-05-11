import type { GameState, Player } from '../../types/game.ts';
import ScoreBar from './ScoreBar.tsx';
import './GameInfo.css';

interface GameInfoProps {
  game: GameState;
  aiThinking: boolean;
  mustPass: boolean;
  onPass: () => void;
  onSurrender: () => void;
}

function colorLabel(player: Player): string {
  return player === 'BLACK' ? 'Black' : 'White';
}

export default function GameInfo({ game, aiThinking, mustPass, onPass, onSurrender }: GameInfoProps) {
  const isHumanTurn = game.currentTurn === game.humanSide;

  return (
    <div className="game-info">
      <div className="game-info__row">
        <span className="game-info__label">You</span>
        <span className={`game-info__disc game-info__disc--${game.humanSide.toLowerCase()}`} />
        <span>{colorLabel(game.humanSide)}</span>
      </div>

      <ScoreBar
        blackCount={game.discCount.BLACK}
        whiteCount={game.discCount.WHITE}
      />

      <div className="game-info__row">
        <span className="game-info__label">Turn</span>
        <span className={`game-info__disc game-info__disc--${game.currentTurn.toLowerCase()}`} />
        <span>{aiThinking ? 'AI thinking...' : isHumanTurn ? 'Your turn' : 'AI turn'}</span>
      </div>

      <div className="game-info__row">
        <span className="game-info__label">Move</span>
        <span>{game.moveNumber}</span>
      </div>

      <div className="game-info__row">
        <span className="game-info__label">Difficulty</span>
        <span>{game.aiDifficulty}</span>
      </div>

      <div className="game-info__row">
        <span className="game-info__label">Variant</span>
        <span>{game.variant}</span>
      </div>

      <div className="game-info__actions">
        {mustPass && isHumanTurn && !aiThinking && (
          <button className="btn btn--pass" onClick={onPass}>
            Pass (no valid moves)
          </button>
        )}
        <button className="btn btn--surrender" onClick={onSurrender} disabled={aiThinking}>
          Surrender
        </button>
      </div>
    </div>
  );
}
