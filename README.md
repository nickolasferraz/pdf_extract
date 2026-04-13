# PDF Extract

Sistema em Java com Spring Boot para upload e processamento de PDFs, com arquitetura em camadas e integração com fluxos de pagamento. O projeto foi pensado para ser limpo, extensível e fácil de evoluir.

## Sobre o projeto

O **PDF Extract** é uma aplicação backend desenvolvida com Java e Spring Boot para receber arquivos PDF, processar o conteúdo e organizar a aplicação de forma clara e estruturada.

O sistema também foi planejado para integrar diferentes formas de pagamento, como **PIX**, **cartão de crédito** e **Checkout Pro**, utilizando os padrões **Factory** e **Strategy** para manter o código desacoplado.

## Funcionalidades

- Upload de arquivos PDF.
- Processamento e extração de conteúdo.
- Estrutura em camadas bem definida.
- Integração com diferentes estratégias de pagamento.
- Suporte para:
  - PIX.
  - Cartão de crédito.
  - Checkout Pro do Mercado Pago.
- Código preparado para fácil manutenção e expansão.

## Tecnologias utilizadas

- Java
- Spring Boot
- Spring Web
- Spring Validation
- Spring Data JPA
- Maven
- PDFBox
- Mercado Pago SDK
- Lombok
- PostgreSQL

## Arquitetura

O projeto segue uma **arquitetura em camadas**, com separação clara entre:

- **Controller**: recebe as requisições HTTP.
- **Service**: contém as regras de negócio.
- **Repository**: acessa o banco de dados.
- **DTOs**: fazem a comunicação entre as camadas.
- **Strategies**: encapsulam as variações de comportamento.
- **Factory**: escolhe dinamicamente a estratégia correta.

Essa estrutura deixa o código mais organizado e reduz o acoplamento entre as partes do sistema.

## Padrões de projeto aplicados

### Factory
Responsável por escolher a estratégia correta de pagamento ou processamento com base no tipo recebido.

### Strategy
Responsável por encapsular os comportamentos diferentes de cada método de pagamento ou processamento, como:

- PIX.
- Cartão de crédito.
- Checkout Pro.

## Exemplo de uso

### Pagamento com PIX
A aplicação recebe a requisição, a factory identifica o tipo de pagamento e direciona para a strategy de PIX.

### Pagamento com cartão
A request é processada pela strategy de cartão, que utiliza os dados necessários para criar o pagamento.

### Checkout Pro
A aplicação cria uma **Preference** no Mercado Pago e retorna a URL para o redirecionamento do usuário.

## Estrutura do projeto

```bash
src/main/java/com/example/pdf_extract
├── controller
├── service
├── repository
├── dto
├── entity
├── factory
└── strategy
