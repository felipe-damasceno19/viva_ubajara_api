-- Migração manual necessária em produção ANTES do deploy da feature de avaliações de visitantes.
-- Motivo: ddl-auto=validate em prod derruba o boot se a tabela tb_review não existir.
-- Ordem: 1) rodar este SQL no Supabase; 2) deploy do backend; 3) deploy do frontend.
-- Idempotente: seguro rodar quantas vezes precisar. Arquivo de apoio, NÃO versionado.
--
-- Colunas conferidas contra Review + BaseEntity (naming do Hibernate):
--   id, spot_id, author_id, rating, comment, status,
--   created_by, created_date, last_modified_by, last_modified_date.
-- PKs referenciadas: tb_user.user_id e tb_tourist_spot.id.

BEGIN;

CREATE TABLE IF NOT EXISTS tb_review (
    id                 uuid         NOT NULL,
    spot_id            uuid         NOT NULL,
    author_id          uuid         NOT NULL,
    rating             integer      NOT NULL,
    comment            varchar(600),
    status             varchar(20)  NOT NULL,
    created_by         varchar(255),
    created_date       timestamp(6) NOT NULL,
    last_modified_by   varchar(255),
    last_modified_date timestamp(6),
    CONSTRAINT pk_review PRIMARY KEY (id)
);

-- ARMADILHA: o Hibernate gera um CHECK com os valores do enum ApprovalStatus. Se um novo valor for
-- adicionado ao enum, esta constraint precisa ser recriada (novo SQL), senão o INSERT falha.
ALTER TABLE tb_review DROP CONSTRAINT IF EXISTS tb_review_status_check;
ALTER TABLE tb_review ADD CONSTRAINT tb_review_status_check
    CHECK (status IN ('PENDING','APPROVED','REJECTED'));

ALTER TABLE tb_review DROP CONSTRAINT IF EXISTS tb_review_rating_check;
ALTER TABLE tb_review ADD CONSTRAINT tb_review_rating_check
    CHECK (rating BETWEEN 1 AND 5);

ALTER TABLE tb_review DROP CONSTRAINT IF EXISTS fk_review_spot;
ALTER TABLE tb_review ADD CONSTRAINT fk_review_spot
    FOREIGN KEY (spot_id) REFERENCES tb_tourist_spot (id) ON DELETE CASCADE;

ALTER TABLE tb_review DROP CONSTRAINT IF EXISTS fk_review_author;
ALTER TABLE tb_review ADD CONSTRAINT fk_review_author
    FOREIGN KEY (author_id) REFERENCES tb_user (user_id) ON DELETE CASCADE;

-- Uma avaliação por usuário e local (o upsert do serviço depende deste índice para fechar a corrida).
ALTER TABLE tb_review DROP CONSTRAINT IF EXISTS ux_review_spot_author;
ALTER TABLE tb_review ADD CONSTRAINT ux_review_spot_author UNIQUE (spot_id, author_id);

CREATE INDEX IF NOT EXISTS ix_review_spot_status_created ON tb_review (spot_id, status, created_date DESC);
CREATE INDEX IF NOT EXISTS ix_review_status_created ON tb_review (status, created_date DESC);

-- Mesmo padrão das outras tabelas (enable_rls_supabase.sql): acesso só pelo backend.
ALTER TABLE tb_review ENABLE ROW LEVEL SECURITY;

COMMIT;

-- Verificação opcional (rodar separado, depois do COMMIT):
-- SELECT column_name, data_type, character_maximum_length, is_nullable
--   FROM information_schema.columns
--  WHERE table_name = 'tb_review'
--  ORDER BY ordinal_position;
