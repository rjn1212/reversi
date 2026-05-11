#!/usr/bin/env bash
ROOT="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$ROOT/reversio-backend"
FRONTEND_DIR="$ROOT/reversio-frontend"

BACKEND_PORT="${BACKEND_PORT:-8084}"
FRONTEND_PORT="${FRONTEND_PORT:-5177}"

echo "==> Starting Reversio"
echo "    Frontend (dev): http://localhost:$FRONTEND_PORT"
echo "    Backend:        http://localhost:$BACKEND_PORT  (also serves static frontend)"
echo "    Ctrl+C to stop."
echo ""

# Clear ports
lsof -ti:"$BACKEND_PORT" | xargs kill -9 2>/dev/null || true
lsof -ti:"$FRONTEND_PORT" | xargs kill -9 2>/dev/null || true
sleep 1

# Build frontend and copy static files to backend
echo "==> Building frontend..."
cd "$FRONTEND_DIR"
npm install --silent
npx vite build
echo "==> Copying static files to backend..."
rm -rf "$BACKEND_DIR/src/main/resources/static"
rm -rf "$BACKEND_DIR/target/classes/static"
cp -r "$FRONTEND_DIR/dist" "$BACKEND_DIR/src/main/resources/static"
mkdir -p "$BACKEND_DIR/target/classes"
cp -r "$FRONTEND_DIR/dist" "$BACKEND_DIR/target/classes/static"
echo "    Done. Remote users can access http://<host>:$BACKEND_PORT"
echo ""

# Start backend
cd "$BACKEND_DIR"
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=$BACKEND_PORT" &
BACKEND_PID=$!

# Start frontend dev server (for local hot-reload)
cd "$FRONTEND_DIR"
npx vite --port "$FRONTEND_PORT" &
FRONTEND_PID=$!

cleanup() {
  echo ""
  echo "Stopping..."
  kill "$BACKEND_PID" "$FRONTEND_PID" 2>/dev/null || true
  exit 0
}
trap cleanup INT TERM
wait
