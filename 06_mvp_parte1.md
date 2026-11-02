# Documentação Técnica do MVP - Parte 1 (Schulz S.A. Metrologia)

## 1. Padrões de POO Utilizados
A arquitetura foi implementada em Java utilizando boas práticas de engenharia de software:
- **Encapsulamento**: Atributos das classes de domínio protegidos com modificadores de acesso privados (`private`) e expostos controladamente através de métodos públicos.
- **Separação de Responsabilidades**: A classe `DatabaseConnection` isola a gerência de conexão via JDBC, enquanto as classes de domínio cuidam de sua própria persistência e regras de negócio.
- **Validação de Regras de Negócio**: A lógica metrológica de aprovação/reprovação ocorre de forma automatizada no construtor da classe `ResultadoMedicao`.

## 2. Diagramação Textual das Classes
- **ItemCalibrado**: Armazena as propriedades do instrumento medido (`cd_instrumento`, `nm_denominacao`).
- **CertificadoCalibracao**: Reúne dados do ensaio, data, validade e o nome do técnico responsável.
- **ResultadoMedicao**: Realiza o cálculo da média e valida o status com base nos limites superior e inferior.

## 3. Tratamento de Erros e Validação
- Uso de blocos `try-with-resources` para fechamento automático de conexões, prevenindo concorrência e travamentos no arquivo `.db` do SQLite.
- Captura estruturada de exceções através do `SQLException`.
