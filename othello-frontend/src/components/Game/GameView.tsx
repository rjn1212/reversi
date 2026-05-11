import type { GameState, Position, MoveData } from '../../types/game.ts';
import Board from '../Board/Board.tsx';
import GameInfo from './GameInfo.tsx';
import './GameView.css';

interface GameViewProps {
  game: GameState;
  validMoves: Position[];
  mustPass: boolean;
  aiThinking: boolean;
  lastHumanMove: MoveData | null;
  lastAiMove: MoveData | null;
  error: string | null;
  onSquareClick: (row: number, col: number) => void;
  onPass: () => void;
  onSurrender: () => void;
}

export default function GameView({
  game, validMoves, mustPass, aiThinking, lastHumanMove, lastAiMove,
  error, onSquareClick, onPass, onSurrender,
}: GameViewProps) {
  const isHumanTurn = game.currentTurn === game.humanSide;
  const disabled = !isHumanTurn || aiThinking || game.phase === 'GAME_OVER';

  return (
    <div className="game-view">
      <div className="game-view__board-area">
        {game.phase === 'PLACEMENT' && (
          <div className="game-view__placement-hint">
            Place a disc in one of the center squares
          </div>
        )}
        <Board
          discs={game.board.discs}
          validMoves={disabled ? [] : validMoves}
          lastHumanMove={lastHumanMove}
          lastAiMove={lastAiMove}
          onSquareClick={onSquareClick}
          disabled={disabled}
        />
        {error && <div className="game-view__error">{error}</div>}
        {aiThinking && <div className="game-view__thinking">AI is thinking...</div>}
      </div>
      <GameInfo
        game={game}
        aiThinking={aiThinking}
        mustPass={mustPass && isHumanTurn && !aiThinking}
        onPass={onPass}
        onSurrender={onSurrender}
      />
    </div>
  );
}
