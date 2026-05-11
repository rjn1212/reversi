import { PlatformClient } from '@gitgames/platform-sdk';
import type { AiDifficulty, GameState, GameVariant, MoveResponse, Player, Position, ValidMovesResponse } from '../types/game.ts';

export const platform = new PlatformClient();

const BASE_URL = '/api/v1';

function getHeaders(): Record<string, string> {
  return {
    'Content-Type': 'application/json',
    ...platform.getAuthHeaders(),
  };
}

export const gameApi = {
  async createGame(aiDifficulty: AiDifficulty, variant: GameVariant, playerSide: Player): Promise<GameState> {
    const res = await fetch(`${BASE_URL}/games`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({ aiDifficulty, variant, playerSide }),
    });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
  },

  async getGame(gameId: string): Promise<GameState> {
    const res = await fetch(`${BASE_URL}/games/${gameId}`, { headers: getHeaders() });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
  },

  async getValidMoves(gameId: string): Promise<ValidMovesResponse> {
    const res = await fetch(`${BASE_URL}/games/${gameId}/moves`, { headers: getHeaders() });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
  },

  async makeMove(gameId: string, position: Position): Promise<MoveResponse> {
    const res = await fetch(`${BASE_URL}/games/${gameId}/moves`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({ position }),
    });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
  },

  async pass(gameId: string): Promise<MoveResponse> {
    const res = await fetch(`${BASE_URL}/games/${gameId}/moves`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({ pass: true }),
    });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
  },

  async surrender(gameId: string): Promise<GameState> {
    const res = await fetch(`${BASE_URL}/games/${gameId}/surrender`, {
      method: 'POST',
      headers: getHeaders(),
    });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
  },

  async abandonGame(gameId: string): Promise<void> {
    await fetch(`${BASE_URL}/games/${gameId}`, {
      method: 'DELETE',
      headers: getHeaders(),
    });
  },
};
