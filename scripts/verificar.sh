#!/usr/bin/env bash
# Pruebas sobre MariaDB desechable. Requiere RabbitMQ local o RABBITMQ_HOST/PORT.
set -euo pipefail
cd "$(dirname "$0")/.."
audit_dir=$(mktemp -d /tmp/ppooii-verificacion.XXXXXX)
audit_pid=''
cleanup() {
  if [ -n "$audit_pid" ]; then kill "$audit_pid" 2>/dev/null || true; wait "$audit_pid" 2>/dev/null || true; fi
  rm -rf "$audit_dir"
}
trap cleanup EXIT
mariadb-install-db --datadir="$audit_dir/db" --auth-root-authentication-method=normal --skip-test-db > "$audit_dir/init.log" 2>&1
# Puerto efímero disponible; no usa el servidor ni los datos de la aplicación.
audit_port=$(python - <<'PY'
import socket
with socket.socket() as s:
 s.bind(('127.0.0.1',0));print(s.getsockname()[1])
PY
)
mariadbd --datadir="$audit_dir/db" --socket="$audit_dir/db.sock" --pid-file="$audit_dir/db.pid" --port="$audit_port" --bind-address=127.0.0.1 --log-error="$audit_dir/server.log" &
audit_pid=$!
for i in $(seq 1 50); do
  if mariadb-admin --socket="$audit_dir/db.sock" -u root ping > /dev/null 2>&1; then break; fi
  sleep 0.1
done
mariadb --socket="$audit_dir/db.sock" -u root < database/script.sql
mariadb --socket="$audit_dir/db.sock" -u root ppooii < database/migracion_requisitos.sql
mariadb --socket="$audit_dir/db.sock" -u root ppooii < database/migracion_requisitos.sql
mariadb --socket="$audit_dir/db.sock" -u root -e "CREATE USER 'audit'@'127.0.0.1' IDENTIFIED BY 'audit'; GRANT ALL ON ppooii.* TO 'audit'@'127.0.0.1';"
TEST_DB_URL="jdbc:mariadb://127.0.0.1:$audit_port/ppooii" TEST_DB_USER=audit TEST_DB_PASSWORD=audit TEST_RABBIT_QUEUE="ppooii.audit.$$" bash mvnw test
