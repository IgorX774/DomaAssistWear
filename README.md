# Doma Assist Wear

Aplicativo demonstrativo nativo para Wear OS, desenvolvido em Kotlin para a atividade DGT2816. A interface oferece verificação de saídas de áudio, detecção de conexão e remoção de dispositivos, atalho para as configurações Bluetooth, leitura de texto em voz alta, comando de voz e alerta sonoro.

## Abrir e executar

1. Instale/abra o Android Studio e escolha **Open** apontando para esta pasta.
2. Aguarde a sincronização do Gradle. O projeto usa Android Gradle Plugin 8.6.1, Kotlin 2.0.20, compileSdk 35 e minSdk 30.
3. Em **Tools > Device Manager**, crie ou selecione um dispositivo Wear OS com API 30 ou superior.
4. Execute a configuração **app** no emulador. Um dispositivo físico pode oferecer resultados de áudio e reconhecimento de fala mais completos.
5. Use os botões da tela. Para simular Bluetooth, emparelhe um fone compatível nas configurações do emulador/dispositivo.

## Comandos de voz

O app delega o reconhecimento à atividade de fala disponível no dispositivo. Tente “verificar áudio”, “alerta” ou “ler mensagem”. Disponibilidade, idioma e interface do serviço de reconhecimento dependem do emulador/dispositivo e dos serviços instalados.

## Escopo e privacidade

O protótipo não coleta nem envia dados. “Ler mensagem” fala somente o texto digitado pelo usuário no app; leitura de notificações de outros aplicativos exigiria um NotificationListenerService e consentimento explícito, não incluídos nesta demonstração. O botão de alerta é demonstrativo: ele não envia uma chamada, mensagem ou notificação remota de emergência.

## Capturas de tela solicitadas no roteiro

As capturas precisam ser feitas depois de executar o app no Android Studio/Emulador Wear OS. Salve imagens da tela principal, status com saída de áudio e alerta em `screenshots/`. Para a captura complementar em relógio pareado, use o menu do app complementar Wear no telefone > **Fazer captura de tela do wearable** e anexe a imagem exportada. Este pacote não contém imagens simuladas como se fossem capturas reais.

## Estrutura

- `app/src/main/`: código Kotlin, manifesto e tema.
- `screenshots/`: local para as capturas reais.
- `DOCUMENTACAO.pdf` (na pasta de entrega): documentação do projeto e roteiro de execução.
