import type { Player } from '../../types/game.ts';
import './Disc.css';

interface DiscProps {
  color: Player;
  flipping?: boolean;
  lastPlaced?: boolean;
}

export default function Disc({ color, flipping, lastPlaced }: DiscProps) {
  const classes = [
    'disc',
    `disc--${color.toLowerCase()}`,
    flipping ? 'disc--flipping' : '',
    lastPlaced ? 'disc--last-placed' : '',
  ].filter(Boolean).join(' ');

  return <div className={classes} />;
}
