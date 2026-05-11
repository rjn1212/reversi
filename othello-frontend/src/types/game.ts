export type Player = 'BLACK' | 'WHITE';
export type GamePhase = 'PLACEMENT' | 'PLAYING' | 'GAME_OVER';
export type GameVariant = 'OTHELLO' | 'REVERSI';
export type AiDifficulty = 'EASY' | 'MEDIUM' | 'HARD';

export interface Position {
  row: number;
  col: number;
}

export interface DiscData {
  position: Position;
  color: Player;
}

export interface BoardData {
  size: number;
  discs: DiscData[];
}

export interface GameState {
  gameId: string;
  phase: GamePhase;
  variant: GameVariant;
  currentTurn: Player;
  humanSide: Player;
  aiDifficulty: AiDifficulty;
  board: BoardData;
  discCount: Record<Player, number>;
  moveNumber: number;
  winner: Player | null;
  winReason: string | null;
}

export interface MoveData {
  position: Position | null;
  flipped: Position[];
  pass: boolean;
}

export interface MoveResponse {
  humanMove: MoveData;
  aiMove: MoveData | null;
  gameState: GameState;
}

export interface ValidMovesResponse {
  validMoves: Position[];
  mustPass: boolean;
}
