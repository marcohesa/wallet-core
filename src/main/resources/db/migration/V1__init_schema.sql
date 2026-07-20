-- ==========================================
-- 1. TABLA DE USUARIOS
-- ==========================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE users IS 'Almacena la información personal de los clientes registrados en la plataforma fintech.';
COMMENT ON COLUMN users.id IS 'Identificador único autoincrementable del usuario.';
COMMENT ON COLUMN users.first_name IS 'Nombre del usuario.';
COMMENT ON COLUMN users.last_name IS 'Apellido del usuario.';
COMMENT ON COLUMN users.email IS 'Correo electrónico único utilizado para autenticación y notificaciones.';
COMMENT ON COLUMN users.created_at IS 'Fecha y hora exactas (con zona horaria) en que se creó la cuenta.';
COMMENT ON COLUMN users.updated_at IS 'Fecha y hora de la última modificación en el perfil del usuario.';


-- ==========================================
-- 2. TABLA DE BILLETERAS (WALLETS)
-- ==========================================
CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    balance NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_balance_positive CHECK (balance >= 0)
);

COMMENT ON TABLE wallets IS 'Contiene las billeteras digitales asociadas a cada usuario y su saldo disponible.';
COMMENT ON COLUMN wallets.id IS 'Identificador único de la billetera.';
COMMENT ON COLUMN wallets.user_id IS 'Clave foránea que vincula la billetera con el usuario propietario.';
COMMENT ON COLUMN wallets.balance IS 'Monto monetario actual. Usa NUMERIC(19,4) para evitar errores de redondeo de punto flotante.';
COMMENT ON COLUMN wallets.currency IS 'Código ISO 4217 de la divisa (ej. USD, MXN, EUR).';
COMMENT ON COLUMN wallets.version IS 'Número de versión utilizado por Hibernate/JPA para control de concurrencia optimista.';
COMMENT ON COLUMN wallets.created_at IS 'Fecha y hora de creación de la billetera.';
COMMENT ON COLUMN wallets.updated_at IS 'Fecha y hora del último movimiento de saldo.';


-- ==========================================
-- 3. TABLA DE TRANSACCIONES (HISTORIAL INMUTABLE)
-- ==========================================
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    source_wallet_id BIGINT,
    target_wallet_id BIGINT,
    amount NUMERIC(19, 4) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reference_id VARCHAR(100) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tx_source_wallet FOREIGN KEY (source_wallet_id) REFERENCES wallets(id),
    CONSTRAINT fk_tx_target_wallet FOREIGN KEY (target_wallet_id) REFERENCES wallets(id)
);

COMMENT ON TABLE transactions IS 'Libro contable inmutable de todos los movimientos de dinero realizados.';
COMMENT ON COLUMN transactions.id IS 'Identificador único de la transacción.';
COMMENT ON COLUMN transactions.source_wallet_id IS 'Billetera de origen de donde sale el dinero (NULL en caso de Depósito inicial).';
COMMENT ON COLUMN transactions.target_wallet_id IS 'Billetera destino a donde entra el dinero (NULL en caso de Retiro).';
COMMENT ON COLUMN transactions.amount IS 'Monto total transferido o procesado en la operación.';
COMMENT ON COLUMN transactions.type IS 'Tipo de operación financiera: DEPOSIT, WITHDRAWAL o TRANSFER.';
COMMENT ON COLUMN transactions.status IS 'Estado del procesamiento: PENDING, COMPLETED o FAILED.';
COMMENT ON COLUMN transactions.reference_id IS 'Código de seguimiento o referencia externa bancaria.';
COMMENT ON COLUMN transactions.created_at IS 'Fecha y hora exacta en que se registró la transacción.';


-- ==========================================
-- 4. TABLA DE CLAVES DE IDEMPOTENCIA
-- ==========================================
CREATE TABLE idempotency_keys (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    response_status INT NOT NULL,
    response_body TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE idempotency_keys IS 'Registro de peticiones procesadas para prevenir duplicaicón de transacciones por reintentos de red.';
COMMENT ON COLUMN idempotency_keys.id IS 'Identificador único del registro de idempotencia.';
COMMENT ON COLUMN idempotency_keys.idempotency_key IS 'Llave única enviada por el cliente en el Header HTTP (ej. UUID).';
COMMENT ON COLUMN idempotency_keys.response_status IS 'Código de estado HTTP de la respuesta original guardada (ej. 200, 201).';
COMMENT ON COLUMN idempotency_keys.response_body IS 'Cuerpo de respuesta en JSON enviado originalmente para responder lo mismo si se repite la petición.';
COMMENT ON COLUMN idempotency_keys.created_at IS 'Fecha y hora de registro de la petición.';


-- ==========================================
-- ÍNDICES DE RENDIMIENTO
-- ==========================================
CREATE INDEX idx_wallets_user ON wallets(user_id);
CREATE INDEX idx_transactions_source ON transactions(source_wallet_id);
CREATE INDEX idx_transactions_target ON transactions(target_wallet_id);