# HemoUbiquitous Monitor

📊 Sistema de Monitoramento de Hemogramas baseado em **computação ubíqua**, **IoT** e **processamento em tempo real**.  
Este projeto foi desenvolvido no contexto da disciplina **Software para Sistemas Ubíquos (INF0483 - UFG)**.

## 🚀 Objetivo
O sistema tem como finalidade **receber, processar e analisar hemogramas** em tempo real, utilizando princípios e tecnologias de **sistemas ubíquos**, incluindo:
- IoT (sensores e dispositivos móveis)
- Middleware e frameworks de integração (MQTT, Kafka, gRPC)
- Processamento de streams em tempo real
- Integração com padrões de saúde (HL7 FHIR)
- Aplicações móveis para visualização e notificações

## 📌 Funcionalidades Principais
- **Recepção FHIR**: Coleta de dados laboratoriais em formato padronizado (HL7 FHIR).
- **Análise Individual**: Detecção de anomalias em parâmetros hematológicos.
- **Base Consolidada**: Persistência local de hemogramas recebidos.
- **Análise Coletiva**: Identificação de padrões em janelas deslizantes.
- **API REST**: Exposição de endpoints para consulta de alertas.
- **Aplicativo Móvel (Android)**: Notificações e interface para acompanhamento dos resultados.
- **Segurança e Privacidade**: Comunicação segura (HTTPS, mTLS).

## 🛠️ Tecnologias Utilizadas
- **Linguagens**: Java / Kotlin / Python
- **Frameworks & Protocolos**: 
  - Android SDK
  - MQTT
  - Apache Kafka
  - gRPC
  - REST API
- **Infraestrutura**:
  - Google Cloud / AWS
  - Banco de dados (PostgreSQL / MongoDB)
- **Padrões de Saúde**:
  - HL7 FHIR

## 📅 Marcos de Desenvolvimento
1. **Marco 1**: Recepção FHIR (subscription + parsing Observation).
2. **Marco 2**: Análise Individual de Hemogramas.
3. **Marco 3**: Persistência e base consolidada.
4. **Marco 4**: Análise coletiva de dados.
5. **API REST** + **App Mobile**.
6. **Entrega Final** com documentação e demonstração.

## 👥 Equipe
Projeto desenvolvido pelos(as) estudantes da disciplina **INF0483 - Software para Sistemas Ubíquos**  
Universidade Federal de Goiás (UFG) - 2025/2  

## 📖 Referências
- [HL7 FHIR](https://www.hl7.org/fhir/)
- [Apache Kafka](https://kafka.apache.org)
- [MQTT](https://mqtt.org)
- [Google Cloud Docs](https://cloud.google.com/docs)
- [AWS Documentation](https://docs.aws.amazon.com)

---
