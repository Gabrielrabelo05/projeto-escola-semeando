# Deploy: Supabase + Render + Vercel

| Parte | Serviço | Plano |
|---|---|---|
| Banco (PostgreSQL) | Supabase | Free |
| API (Spring Boot, Docker) | Render | Free |
| Web (Next.js) | Vercel | Hobby |

A Vercel não executa servidores Java, por isso a API fica no Render. Os dados sintéticos (RN-08)
continuam ligados (`SEED_ENABLED=true`): na primeira subida, com o banco vazio, o seed cria os usuários
`@semeando.dev` com a senha `Semeando@2026`. Nas subidas seguintes, ele não roda de novo.

Faça os passos **nesta ordem**: a URL de cada serviço é usada pelo seguinte.

---

## 1. Supabase (banco)

1. Em <https://supabase.com/dashboard>, clique em **New project**.
   - Region: **East US (North Virginia)**, a mesma do Render, para diminuir a latência.
   - Anote a senha do banco.
2. No **SQL Editor**, rode o script abaixo. Ele impede que a Data API do Supabase (chaves `anon` e
   `authenticated`) leia as tabelas da aplicação, que incluem hashes de senha e dados pessoais.

   ```sql
   alter default privileges for role postgres in schema public revoke all on tables    from anon, authenticated;
   alter default privileges for role postgres in schema public revoke all on sequences from anon, authenticated;
   alter default privileges for role postgres in schema public revoke all on functions from anon, authenticated;
   revoke all on all tables    in schema public from anon, authenticated;
   revoke all on all sequences in schema public from anon, authenticated;
   ```

3. Clique em **Connect**, aba **JDBC**, e escolha **Session pooler**. Use esse modo, não o *Direct
   connection*: a conexão direta é só IPv6, e o Render não acessa IPv6. Separe os três valores:
   - `DB_URL`: `jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require`
     (copie o host exato do painel);
   - `DB_USER`: `postgres.<id-do-projeto>`;
   - `DB_PASSWORD`: a senha do passo 1.

## 2. Render (API)

1. Gere a chave de criptografia dos dados sensíveis e guarde-a:

   ```bash
   openssl rand -base64 32
   ```

   > Não troque essa chave depois que houver dados, porque os campos já criptografados ficariam ilegíveis.

2. Em <https://dashboard.render.com>, vá em **New → Blueprint**, conecte o GitHub e escolha
   `Gabrielrabelo05/projeto-escola-semeando`. O Render lê o `render.yaml` e pede:

   | Variável | Valor |
   |---|---|
   | `DB_URL`, `DB_USER`, `DB_PASSWORD` | do passo 1.3 |
   | `DATA_KEY` | saída do `openssl` acima |
   | `CORS_ORIGINS` | por enquanto `http://localhost:3000`; será trocado no passo 4 |

   `JWT_SECRET` e `CORA_WEBHOOK_SECRET` são gerados pelo próprio Render.

3. Espere o deploy terminar, que leva de 5 a 10 minutos no primeiro build. No log devem aparecer
   `Migrating schema "public" to version "1 - esquema inicial"` e `Dados sintéticos carregados`.
4. Teste a API: `https://semeando-api.onrender.com/actuator/health` deve responder `{"status":"UP"}`.
   Anote a URL, que pode ter um sufixo diferente.

## 3. Vercel (web)

1. Em <https://vercel.com/new>, importe o mesmo repositório.
2. Em **Root Directory**, escolha `frontend`. O framework (Next.js) e o pnpm são detectados automaticamente.
3. Em **Environment Variables**:
   - `NEXT_PUBLIC_API_URL` = `https://semeando-api.onrender.com/api` (URL do passo 2.4 + `/api`).
4. Clique em **Deploy** e anote a URL, por exemplo `https://projeto-escola-semeando.vercel.app`.

> `NEXT_PUBLIC_API_URL` (ou `BACKEND_URL`, que tem prioridade) é o **destino do proxy `/api`** do Next: o
> navegador só fala com o domínio da Vercel, e o cookie de sessão HttpOnly fica de primeira parte. Ela entra
> no build; se mudar, é preciso fazer um *Redeploy* na Vercel.

## 4. Liberar o front na API (CORS)

No Render, abra **semeando-api → Environment** e troque `CORS_ORIGINS` pela URL da Vercel, sem barra no
final. Para aceitar mais de uma origem, separe por vírgula:

```
https://projeto-escola-semeando.vercel.app,http://localhost:3000
```

Salve. O Render reinicia a API sozinho. Depois, entre no site da Vercel com `admin@semeando.dev` / `Semeando@2026`.

---

## Limitações dos planos grátis

- **Render hiberna** depois de 15 minutos sem acesso. A primeira requisição seguinte leva cerca de 1 minuto.
  Enquanto a API hiberna, as rotinas agendadas não rodam (boletos no dia 1 às 06h e alertas diários às
  07h). Para uma apresentação, abra o `/actuator/health` uns minutos antes.
- **Os uploads somem** a cada deploy ou reinício do Render, porque o disco é temporário. O banco
  (Supabase) não é afetado.
- **O Supabase pausa** o projeto depois de 7 dias sem uso. Para reativar, use o botão *Restore* no painel.
- **A Cora continua fake** (`CORA_MODE=fake`). Os boletos são simulados até existirem credenciais reais.

## Problemas comuns

| Sintoma | Causa provável |
|---|---|
| Login falha com "Network Error" no navegador | `CORS_ORIGINS` sem a URL exata da Vercel, ou `NEXT_PUBLIC_API_URL` errada (exige redeploy) |
| `Connection refused` / `Network is unreachable` no log do Render | `DB_URL` usando *Direct connection* em vez de *Session pooler* |
| `password authentication failed` | `DB_USER` sem o sufixo `.<id-do-projeto>` |
| `Found non-empty schema(s) "public"` | o schema `public` já tinha tabelas; use um projeto Supabase novo |
| `Max client connections reached` | diminua `DB_POOL_SIZE` no Render |
