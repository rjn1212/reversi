import { useGame } from './hooks/useGame.ts';
import NewGameDialog from './components/Common/NewGameDialog.tsx';
import GameView from './components/Game/GameView.tsx';
import GameOverDialog from './components/Common/GameOverDialog.tsx';

function App() {
  const {
    game, validMoves, mustPass, loading, error, aiThinking,
    lastHumanMove, lastAiMove,
    newGame, makeMove, pass, surrender, resetGame,
  } = useGame();

  const handleSquareClick = (row: number, col: number) => {
    if (!game || game.phase === 'GAME_OVER') return;
    const position = { row, col };
    const isValid = validMoves.some(m => m.row === row && m.col === col);
    if (isValid) {
      makeMove(position);
    }
  };

  if (!game) {
    return (
      <div className="app">
        <h1>Othello</h1>
        <NewGameDialog onStart={newGame} loading={loading} />
      </div>
    );
  }

  return (
    <div className="app">
      <div className="app__header">
        <h1>{game.variant === 'OTHELLO' ? 'Othello' : 'Reversi'}</h1>
        <button className="btn app__new-game-btn" onClick={resetGame}>
          New Game
        </button>
      </div>
      <GameView
        game={game}
        validMoves={validMoves}
        mustPass={mustPass}
        aiThinking={aiThinking}
        lastHumanMove={lastHumanMove}
        lastAiMove={lastAiMove}
        error={error}
        onSquareClick={handleSquareClick}
        onPass={pass}
        onSurrender={surrender}
      />
      {game.phase === 'GAME_OVER' && (
        <GameOverDialog game={game} onNewGame={resetGame} />
      )}
    </div>
  );
}

export default App
