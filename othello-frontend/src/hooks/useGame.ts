import { useReducer, useCallback, useEffect, useRef } from 'react';
import type { GameState, MoveData, Position, AiDifficulty, GameVariant, Player } from '../types/game.ts';
import { gameApi } from '../api/gameApi.ts';

interface GameStore {
  game: GameState | null;
  validMoves: Position[];
  mustPass: boolean;
  loading: boolean;
  error: string | null;
  aiThinking: boolean;
  lastHumanMove: MoveData | null;
  lastAiMove: MoveData | null;
}

type Action =
  | { type: 'SET_LOADING' }
  | { type: 'SET_ERROR'; error: string }
  | { type: 'CLEAR_ERROR' }
  | { type: 'SET_GAME'; game: GameState }
  | { type: 'SET_VALID_MOVES'; moves: Position[]; mustPass: boolean }
  | { type: 'AI_THINKING' }
  | { type: 'MOVE_COMPLETE'; game: GameState; humanMove: MoveData; aiMove: MoveData | null }
  | { type: 'CLEAR_MOVE_HIGHLIGHTS' }
  | { type: 'RESET' };

const initialState: GameStore = {
  game: null,
  validMoves: [],
  mustPass: false,
  loading: false,
  error: null,
  aiThinking: false,
  lastHumanMove: null,
  lastAiMove: null,
};

function reducer(state: GameStore, action: Action): GameStore {
  switch (action.type) {
    case 'SET_LOADING':
      return { ...state, loading: true, error: null };
    case 'SET_ERROR':
      return { ...state, loading: false, error: action.error, aiThinking: false };
    case 'CLEAR_ERROR':
      return { ...state, error: null };
    case 'SET_GAME':
      return { ...state, game: action.game, loading: false, aiThinking: false, validMoves: [], mustPass: false, lastHumanMove: null, lastAiMove: null };
    case 'SET_VALID_MOVES':
      return { ...state, validMoves: action.moves, mustPass: action.mustPass };
    case 'AI_THINKING':
      return { ...state, aiThinking: true, validMoves: [], mustPass: false };
    case 'MOVE_COMPLETE':
      return { ...state, game: action.game, aiThinking: false, validMoves: [], mustPass: false, lastHumanMove: action.humanMove, lastAiMove: action.aiMove };
    case 'CLEAR_MOVE_HIGHLIGHTS':
      return { ...state, lastHumanMove: null, lastAiMove: null };
    case 'RESET':
      return initialState;
    default:
      return state;
  }
}

export function useGame() {
  const [state, dispatch] = useReducer(reducer, initialState);
  const errorTimerRef = useRef<number | null>(null);

  useEffect(() => {
    if (state.error) {
      if (errorTimerRef.current) clearTimeout(errorTimerRef.current);
      errorTimerRef.current = window.setTimeout(() => {
        dispatch({ type: 'CLEAR_ERROR' });
      }, 4000);
    }
    return () => {
      if (errorTimerRef.current) clearTimeout(errorTimerRef.current);
    };
  }, [state.error]);

  useEffect(() => {
    if (state.game && state.game.phase !== 'GAME_OVER' && !state.aiThinking && !state.loading) {
      fetchValidMoves();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [state.game?.moveNumber, state.game?.phase]);

  const fetchValidMoves = useCallback(async () => {
    if (!state.game || state.game.phase === 'GAME_OVER') return;
    try {
      const response = await gameApi.getValidMoves(state.game.gameId);
      dispatch({ type: 'SET_VALID_MOVES', moves: response.validMoves, mustPass: response.mustPass });
    } catch (e) {
      dispatch({ type: 'SET_ERROR', error: (e as Error).message });
    }
  }, [state.game]);

  const newGame = useCallback(async (difficulty: AiDifficulty, variant: GameVariant, side: Player) => {
    dispatch({ type: 'SET_LOADING' });
    try {
      const game = await gameApi.createGame(difficulty, variant, side);
      dispatch({ type: 'SET_GAME', game });
    } catch (e) {
      dispatch({ type: 'SET_ERROR', error: (e as Error).message });
    }
  }, []);

  const makeMove = useCallback(async (position: Position) => {
    if (!state.game) return;
    dispatch({ type: 'AI_THINKING' });
    try {
      const response = await gameApi.makeMove(state.game.gameId, position);
      dispatch({
        type: 'MOVE_COMPLETE',
        game: response.gameState,
        humanMove: response.humanMove,
        aiMove: response.aiMove,
      });
    } catch (e) {
      dispatch({ type: 'SET_ERROR', error: (e as Error).message });
    }
  }, [state.game]);

  const pass = useCallback(async () => {
    if (!state.game) return;
    dispatch({ type: 'AI_THINKING' });
    try {
      const response = await gameApi.pass(state.game.gameId);
      dispatch({
        type: 'MOVE_COMPLETE',
        game: response.gameState,
        humanMove: response.humanMove,
        aiMove: response.aiMove,
      });
    } catch (e) {
      dispatch({ type: 'SET_ERROR', error: (e as Error).message });
    }
  }, [state.game]);

  const surrender = useCallback(async () => {
    if (!state.game) return;
    try {
      const game = await gameApi.surrender(state.game.gameId);
      dispatch({ type: 'SET_GAME', game });
    } catch (e) {
      dispatch({ type: 'SET_ERROR', error: (e as Error).message });
    }
  }, [state.game]);

  const resetGame = useCallback(() => {
    dispatch({ type: 'RESET' });
  }, []);

  const clearMoveHighlights = useCallback(() => {
    dispatch({ type: 'CLEAR_MOVE_HIGHLIGHTS' });
  }, []);

  return {
    ...state,
    newGame,
    makeMove,
    pass,
    surrender,
    resetGame,
    clearMoveHighlights,
  };
}
