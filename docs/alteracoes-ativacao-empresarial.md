# Mudanças recentes no backend empresarial

Este documento explica, em termos simples, as mudanças no backend ligadas ao acesso empresarial e ao cadastro de estabelecimentos.

## Ativar uma conta empresarial existente

Uma pessoa que já tem uma conta comum pode ativar o acesso empresarial sem criar outra conta. O backend recebe o nome da empresa e o CNPJ pela rota `POST /auth/activate-company` e usa o e-mail da sessão autenticada para localizar a conta.

Na ativação, a conta passa para o perfil empresarial e recebe os dados da empresa. Nome pessoal, e-mail, usuário, senha e foto continuam associados à mesma conta. Uma conta de administrador ou guia não pode usar essa opção.

Se os mesmos dados forem enviados novamente para uma conta que já foi ativada, a operação é tratada como repetição segura. A conta não é duplicada. Uma tentativa de trocar os dados empresariais depois da ativação é recusada.

## Conferência do CNPJ

- O backend aceita o CNPJ com ou sem pontuação e o guarda em formato normalizado.
- São conferidos os dígitos verificadores, inclusive no formato alfanumérico previsto para novos CNPJs.
- O sistema procura também por versões equivalentes para evitar duplicidade entre valores formatados e sem pontuação.
- A conta é bloqueada durante a ativação para reduzir disputas simultâneas. A unicidade no banco continua sendo a proteção final contra duas contas usando o mesmo CNPJ.
- Erros de dados já cadastrados nos fluxos de cadastro e ativação empresarial retornam uma mensagem mais clara.

## Envio de fotos pelo painel da empresa

A rota autenticada `POST /companies/me/establishment/photos` recebe uma imagem e a ordem de exibição opcional. O backend procura o restaurante ou a hospedagem associados à conta que está conectada e anexa a imagem a esse estabelecimento. Se a conta ainda não cadastrou um estabelecimento, o envio é recusado.

O arquivo passa pelas validações de imagem e armazenamento que o sistema já usa. As permissões da área empresarial garantem que a empresa só envie fotos para o próprio cadastro.

## Outros ajustes

- Foi adicionada a dependência Spring Boot Actuator, usada para recursos de acompanhamento da aplicação. A configuração de quais verificações ficam publicamente disponíveis depende do ambiente de execução.
- Foram acrescentados testes para a validação de CNPJ, ativação da conta e uso da identidade autenticada na rota.

## Arquivos principais

- `AuthController`, `AuthService` e `UserService`: recebem e executam a ativação da conta existente.
- `CompanyController` e `CompanyService`: recebem as fotos da empresa e as associam ao estabelecimento do proprietário autenticado.
- `CompanyActivationRequestDTO` e `CnpjValidator`: validam e padronizam os dados da empresa.
- `UserRepository`: procura CNPJs equivalentes e bloqueia a conta durante a ativação.
- `GlobalExceptionHandler`: apresenta uma resposta adequada para conflitos de cadastro empresarial.
- `src/test/java/.../service` e `AuthControllerActivationSecurityTest`: verificam os cenários adicionados.

## Verificação

Os testes novos foram adicionados ao projeto, mas não foram executados durante esta revisão. O commit reúne somente as mudanças do backend e este documento.
