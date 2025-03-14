# Kumulus
## _Desafio - Cadastro de Pessoas_

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white) ![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white) ![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white) ![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white) ![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)

Este é um projeto simples, de cadastro de pessoas e endereços, que demonstra uma aplicação web desenvolvida usando a tecnologia Java, com a especificação JavaServer Faces (JSF), aplicando uma arquitetura MVC. Logo abaixo foram detalhadas as tecnologias e plugins utilizados, as decisões técnicas adotadas. Além de orientações para configuração e execução do projeto.

### Desenvolvido por:

- Daniel Olinto de Araújo
- Disponibilizado em <https://github.com/danielolinto31/kumulus>

### Tecnologias e ferramentas utilizadas:

- **[Java JDK:8]:** _Linguagem de programação par projetos backend._
- **[Eclipse IDE:2025-03]:** _Ferramenta utilizada para desenvolvimento do projeto Java._
- **[Maven:3.9.9]:** _Ferramenta de automação de build e gerenciamento de dependências._
- **Spring:3.2.X:** _Framework Java criado com o objetivo de facilitar a configuração e desenvolvimento de código._
- **[Lombok:1.18.36]:** _Biblioteca que permite escrever código eliminando a verbosidade._
- **[PrimeFaces:13.0.0]:** _Biblioteca de componentes UI para aplicações JavaServer Faces (JSF)._
- **JPA / Hibernate:** _Framework para o mapeamento objeto-relacional escrito na linguagem Java._
- **[PostgreSQL:16.8]:** _Banco de dados relacional._
- **[Tomcat:9.0.102]:** _Container de servlets, utilizado como servidor web Java._
- **SonarQube:** _Ferramenta de revisão de código, com capacidade de detectar bugs, código duplicado, entre outros problemas no código._

### Plugins instalados na IDE:

- **[Lombok:1.18.36]**
- **SonarQube (SonarLint):** MarketPlace do Eclipse IDE

### Decisões técnicas e arquiteturais, e bônus:

- Arquitetura MVC.
- Adotadas medidas de segurança com:
    - Content Security Policy (CSP):
        - Padrão de segurança introduzido para evitar cross-site scripting (XSS), clickjacking e outros ataques de injeção de código resultantes da execução de conteúdo malicioso no contexto de página da web confiável.
        - Mais informações em: <https://primefaces.github.io/primefaces/13_0_0/#/core/contentsecuritypolicy>
    - reCAPTCHA:
        - Componente de validação de formulário baseado na API Recaptcha V2.
- Configuradas variáveis de ambiente no Tomcat:
    - Boa prática para evitar que dados sensíveis e credenciais fiquem expostos no código.
- Realizadas validações dos campos, sendo:
    - Definição de campos obrigatórios e opcionais.
    - Não permitir preencher data de nascimento futuras.
    - Não permitir salvar a pessoa sem informar pelo menos 1 (um) endereço.
    - Validações no campo CEP, sendo obrigatório e válido.
    - Só é permitido salvar um CPF por pessoa.
- Configurado o SonarQube na IDE para desenvolver aplicando boas práticas de codificação desde o início.
- Adicionados filtros e ordenação nas tabelas, bem como, utilizado lazy para consumir somente os dados necessários por página.
- Adicionada possibilidade de exportar os dados das tabelas em arquivos nos formatos PDF e CSV.
- Além da data de nascimento da pessoa, também é salvo em banco de dados a idade (Integer).
- Também é salvo o IP e a data/hora que a pessoa foi cadastrada.
- Por fim, foram criadas as páginas `404.xhtml` e `error.xhtml`. Caso tente acessar uma página que não existe ou ocorra algum erro, o sistema redireciona para essas páginas.
- Para o preenchimento do endereço, foi utilizado o [ViaCep], um webservice gratuito de alto desempenho para consulta de Código de Endereçamento Postal (CEP) do Brasil.

### Configuração do ambiente de desenvolvimento:

1. Faça o download dos _softwares_:
    - [Java JDK:8], [Eclipse IDE:2025-03], [Maven:3.9.9], [Tomcat:9.0.102], [PostgreSQL:16.8]
2. Usando Sistema Operacional Windows, configure as variáveis de ambiente: `JAVA_HOME` e `M2_HOME`:
    ```
    JAVA_HOME=C:\Program Files\Java\jdk1.8.0_202
    M2_HOME=C:\apache-maven-3.9.9
    
    ## Na variável "Path" adicione:
    %JAVA_HOME%\bin
    %M2_HOME%\bin
    ```
3. Execute o script para criação das tabelas, sequences e index:
    ```sql
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
    ```
    _Obs.: O script também está disponível em `/kumulus/src/main/webapp/resources/sql/script.sql`_
4. Abra o Eclipse IDE:
    - Vá no menu `Help` -> `Install New Software...` e instale o [Lombok:1.18.36].
    - Vá novamente no menu `Help` -> `Eclipse Marketplace...` e `instale SonarQube for IDE 11.2`.
    - Configura o `Tomcat v9.0 Server` e faça as seguintes alterações:
        - Desabilitar a opção `Modules auto reload by default`.
            - É opcional, mas evita que fique reiniciando o Tomcat a qualquer mudança nas classes Java.
        - Clicar em `Open launch configuration` -> Aba `Environment` e adicionar as variáveis:
    ```
    APP_URL=http://localhost:8080/kumulus
    APP_LOGO=icon-kumulus.png
    DB_APP_URL=jdbc:postgresql://localhost:5432/kumulus
    DB_APP_USER=postgres
    DB_APP_PASS=
    DB_APP_DRIVER=org.postgresql.Driver
    DB_APP_DIALECT=org.hibernate.dialect.PostgreSQLDialect
    DB_APP_HBM2DDL=update
    DB_APP_SHOW_SQL=false
    DB_APP_MIN_POOL_SIZE=2
    DB_APP_MAX_POOL_SIZE=10
    DB_APP_ACQUIRE_INCREMENT=1
    DB_APP_MAX_IDLE_TIME=200
    ```
    _Obs.: Verificar se a URL, User e Password estão configurados corretamente._
    - Faça o _download_ do projeto (`kumulus.zip` ou `kumulus.war`) e descompacte no diretório do workspace do Eclipse ou no git.
        - O projeto também foi disponibilizado no GitHub: 
    - Importe o projeto no Eclipse selecionando a opção `Existing Maven Projects`.
5. No menu, procurar o ícone do `Run maven install`, clicar em `External Tools Configurations...` e preencher em `Program`:
	- Name: `maven install`.
	- Location: `C:\apache-maven-3.9.9\bin\mvn.cmd`.
	- Working Directory: `${project_loc}`.
	- Arguments: `clean eclipse:eclipse install`.
	- Após preenchido, clicar em `Apply` e `Run` para fazer o _build_ do projeto.
6. Caso o projeto não rode por não encontrar as classes `org.springframework.web.context.ContextLoaderListener` e `org.springframework.web.context.request.RequestContextListener`, faça o seguinte:
    - Clique com o botão direito em cima do projeto e vá em `Properties`.
    - Pesquise ou ache a opção `Deployment Assembly`.
    - Clique em `Add...` -> `Java Build Path Entries`, adicione todas as dependências e clique em `Finish`.
7. Agora é possível executar o projeto, basta iniciar o servidor Tomcat dentro do Eclipse IDE.
    - Caso execute com sucesso, basta colocar a URL no navegador: <http://localhost:8080/kumulus/>

[//]: # (Referências de links)

   [Java JDK:8]: <https://www.oracle.com/br/java/technologies/javase/javase8-archive-downloads.html>
   [Eclipse IDE:2025-03]: <https://www.eclipse.org/downloads/packages/>
   [Maven:3.9.9]: <https://maven.apache.org/download.cgi>
   [Tomcat:9.0.102]: <https://tomcat.apache.org/download-90.cgi>
   [PostgreSQL:16.8]: <https://www.postgresql.org/download/windows/>
   [Lombok:1.18.36]: <https://projectlombok.org/setup/eclipse>
   [PrimeFaces:13.0.0]: <https://primefaces.github.io/primefaces/13_0_0/#/>
   [Git:Latest]: <https://git-scm.com/downloads>
   
   [ViaCep]: <https://viacep.com.br/>
