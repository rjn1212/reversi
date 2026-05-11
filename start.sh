#!/usr/bin/env bash
set -e

# Kill existing servers on our ports
lsof -ti:8084 | xargs kill 2>/dev/null || true
lsof -ti:5177 | xargs kill 2>/dev/null || true

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Starting Othello backend on :8084..."
cd "$SCRIPT_DIR/othello-backend"
mvn spring-boot:run > /tmp/othello-backend.log 2>&1 &
BACKEND_PID=$!

echo "Starting Othello frontend on :5177..."
cd "$SCRIPT_DIR/othello-frontend"
npm run dev > /tmp/othello-frontend.log 2>&1 &
FRONTEND_PID=$!

cleanup() {
  echo ""
  echo "Stopping servers..."
  kill $BACKEND_PID $FRONTEND_PID 2>/dev/null
  wait $BACKEND_PID $FRONTEND_PID 2>/dev/null
  echo "Done."
}
trap cleanup EXIT INT TERM

echo ""
echo "Backend:  http://localhost:8084"
echo "Frontend: http://localhost:5177"
echo ""
echo "Press Ctrl+C to stop both servers."
echo ""

tail -f /tmp/othello-backend.log /tmp/othello-frontend.log
