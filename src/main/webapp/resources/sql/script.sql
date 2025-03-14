CREATE TABLE IF NOT EXISTS public.pessoa
(
    id integer NOT NULL,
    cpf character varying(14) COLLATE pg_catalog."default" NOT NULL,
    data_envio timestamp without time zone,
    data_nascimento date NOT NULL,
    idade integer NOT NULL,
    ip character varying(255) COLLATE pg_catalog."default",
    nome character varying(150) COLLATE pg_catalog."default" NOT NULL,
    sexo "char" NOT NULL,
    CONSTRAINT pessoa_pkey PRIMARY KEY (id)
)

CREATE TABLE IF NOT EXISTS public.endereco
(
    id integer NOT NULL,
    bairro character varying(100) COLLATE pg_catalog."default" NOT NULL,
    cep character varying(8) COLLATE pg_catalog."default" NOT NULL,
    cidade character varying(100) COLLATE pg_catalog."default" NOT NULL,
    complemento character varying(100) COLLATE pg_catalog."default",
    logradouro character varying(100) COLLATE pg_catalog."default" NOT NULL,
    numero integer NOT NULL,
    uf character varying(2) COLLATE pg_catalog."default" NOT NULL,
    id_pessoa integer,
    CONSTRAINT endereco_pkey PRIMARY KEY (id),
    CONSTRAINT fk_id_pessoa FOREIGN KEY (id_pessoa)
        REFERENCES public.pessoa (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

CREATE SEQUENCE IF NOT EXISTS public.pessoa_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS public.endereco_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE INDEX IF NOT EXISTS idx_id_pessoa
    ON public.endereco USING btree
    (id_pessoa ASC NULLS LAST)
    WITH (deduplicate_items=True)
    TABLESPACE pg_default;

ALTER SEQUENCE public.pessoa_sequence
    OWNER TO postgres;

ALTER SEQUENCE public.endereco_sequence
    OWNER TO postgres;

ALTER TABLE IF EXISTS public.endereco
    OWNER to postgres;

ALTER TABLE IF EXISTS public.pessoa
    OWNER to postgres;