import type { GameState } from '../../types/game.ts';
import './GameOverDialog.css';

interface GameOverDialogProps {
  game: GameState;
  onNewGame: () => void;
}

export default function GameOverDialog({ game, onNewGame }: GameOverDialogProps) {
  const blackCount = game.discCount.BLACK;
  const whiteCount = game.discCount.WHITE;
  const isWinner = game.winner === game.humanSide;
  const isDraw = game.winner === null;

  let title: string;
  if (isDraw) {
    title = "It's a Draw!";
  } else if (isWinner) {
    title = 'You Win!';
  } else {
    title = 'You Lose';
  }

  return (
    <div className="game-over-overlay">
      <div className="game-over-dialog">
        <h2 className={`game-over-dialog__title ${isDraw ? 'draw' : isWinner ? 'win' : 'lose'}`}>
          {title}
        </h2>
        <div className="game-over-dialog__score">
          <div className="game-over-dialog__side">
            <div className="game-over-dialog__disc game-over-dialog__disc--black" />
            <span>{blackCount}</span>
          </div>
          <span className="game-over-dialog__dash">-</span>
          <div className="game-over-dialog__side">
            <span>{whiteCount}</span>
            <div className="game-over-dialog__disc game-over-dialog__disc--white" />
          </div>
        </div>
        {game.winReason && (
          <p className="game-over-dialog__reason">{game.winReason}</p>
        )}
        <button className="btn btn--primary" onClick={onNewGame}>
          New Game
        </button>
      </div>
    </div>
  );
}
