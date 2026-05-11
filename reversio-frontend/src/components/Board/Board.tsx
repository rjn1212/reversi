import type { DiscData, Position, MoveData } from '../../types/game.ts';
import Disc from '../Disc/Disc.tsx';
import './Board.css';

interface BoardProps {
  discs: DiscData[];
  validMoves: Position[];
  lastHumanMove: MoveData | null;
  lastAiMove: MoveData | null;
  onSquareClick: (row: number, col: number) => void;
  disabled?: boolean;
}

const COL_LABELS = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'];

function posMatch(a: Position, b: Position): boolean {
  return a.row === b.row && a.col === b.col;
}

function isInList(list: Position[], row: number, col: number): boolean {
  return list.some(p => p.row === row && p.col === col);
}

export default function Board({ discs, validMoves, lastHumanMove, lastAiMove, onSquareClick, disabled }: BoardProps) {
  const flippedPositions: Position[] = [
    ...(lastHumanMove?.flipped ?? []),
    ...(lastAiMove?.flipped ?? []),
  ];

  const lastPlacedPositions: Position[] = [];
  if (lastHumanMove?.position) lastPlacedPositions.push(lastHumanMove.position);
  if (lastAiMove?.position) lastPlacedPositions.push(lastAiMove.position);

  return (
    <div className="board-wrapper">
      <div className="board-col-labels">
        {COL_LABELS.map(label => (
          <div key={label} className="board-label">{label}</div>
        ))}
      </div>
      <div className="board-main">
        <div className="board-row-labels">
          {Array.from({ length: 8 }, (_, i) => (
            <div key={i} className="board-label">{i + 1}</div>
          ))}
        </div>
        <div className="board">
          {Array.from({ length: 64 }, (_, i) => {
            const row = Math.floor(i / 8);
            const col = i % 8;
            const disc = discs.find(d => d.position.row === row && d.position.col === col);
            const isValid = isInList(validMoves, row, col);
            const isFlipped = isInList(flippedPositions, row, col);
            const isLastPlaced = lastPlacedPositions.some(p => posMatch(p, { row, col }));

            const squareClasses = [
              'board__square',
              isValid && !disabled ? 'board__square--valid' : '',
            ].filter(Boolean).join(' ');

            return (
              <div
                key={i}
                className={squareClasses}
                onClick={() => !disabled && onSquareClick(row, col)}
              >
                {disc && (
                  <Disc
                    color={disc.color}
                    flipping={isFlipped}
                    lastPlaced={isLastPlaced}
                  />
                )}
                {isValid && !disc && !disabled && (
                  <div className="board__valid-dot" />
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
