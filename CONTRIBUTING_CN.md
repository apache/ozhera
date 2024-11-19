<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->

# 为 Apache OzHera(incubating) 做贡献

如果您有兴趣寻找关于 Apache OzHera(incubating) 的漏洞，或是希望帮助提升项目质量，我们会非常欢迎您的加入。以下是为您提供的贡献指南列表。

[[English Contributing Document](./CONTRIBUTING.md)]

## 目录

* [报告安全问题](#报告安全问题)
* [报告一般问题](#报告一般问题)
* [代码和文档贡献](#代码和文档贡献)
* [测试用例贡献](#测试用例贡献)
* [参与帮助任何事项](#参与帮助任何事项)
* [代码风格](#代码风格)

## 报告安全问题

安全问题是我们最关注的事情。我们建议您不要公开讨论或发布任何关于 Apache OzHera(incubating) 的安全漏洞。如果您发现安全问题，请直接发送电子邮件至 [private@ozhera.apache.org](mailto:private@ozhera.apache.org) 以保密的方式报告。

## 报告一般问题

用户的反馈是项目进步的重要来源。在使用 Apache OzHera(incubating) 后，您可以通过 [NEW ISSUE](https://github.com/apache/ozhera/issues/new/choose) 提交您的问题或建议。为确保沟通效率，我们建议先搜索是否已存在相关问题，然后在已有问题下添加您的详细信息，而不是创建新问题。

当您遇到以下情况，可以新建一个问题：

* 错误报告
* 功能请求
* 性能问题
* 功能设计或改进建议
* 测试改进
* 文档不完整
* 其他项目相关的问题

请注意，提交问题时请删除任何敏感数据，例如密码、密钥、网络地址等。

## 代码和文档贡献

我们欢迎一切可以让 Apache OzHera(incubating) 项目更好的贡献。在 GitHub 上，所有的改进都可以通过 PR（Pull Request）实现：

* 如果您发现错别字或错误，请修复它！
* 如果发现功能可以优化，请提交改进！
* 如果文档不准确或不完整，欢迎您进行更新！

> 我们期待您的任何PR。

请注意 Apache OzHera(incubating) 项目对 PR 的要求，并在以下指南下提交：

* [工作区准备](#工作区准备)
* [分支定义](#分支定义)
* [提交规则](#提交规则)
* [PR说明](#PR说明)

### 工作区准备

请首先 Fork 项目，然后克隆到本地计算机进行开发。设置远程仓库后，您可以轻松地与上游分支同步代码。

### 分支定义

Apache OzHera(incubating) 项目使用以下几种分支类型：

* **开发分支**：用于所有新功能和改进的开发
* **发布分支**：发布版本时创建
* **热修复分支**：用于修复发布版本中的紧急问题

### 提交规则

我们非常重视提交的质量，包括：

* 提交消息：请确保使用明确的提交信息，例如 `docs: update installation guide`
* 提交内容：提交应包含完整且可审查的内容，并与 GitHub ID 相关联

### PR说明

PR 是更改 Apache OzHera(incubating) 项目文件的主要方式。我们建议使用 [PR 模板](./.github/PULL_REQUEST_TEMPLATE.md) 来描述更改内容。

## 测试用例贡献

Apache OzHera(incubating) 项目优先接收功能测试用例贡献。推荐使用 JUnit 进行单元测试，并使用 Mockito 进行集成测试。

## 参与帮助任何事项

Apache OzHera(incubating) 项目通过 GitHub 进行协作，我们鼓励贡献者通过以下方式参与：

* 回答他人的问题
* 帮助审查他人的 PR 设计和代码
* 讨论项目改进建议
* 撰写关于 Apache OzHera(incubating) 的博客或分享使用经验

## 代码风格

Apache OzHera(incubating) 项目遵循阿里巴巴 Java 编码指南。

### 指南

[阿里巴巴 Java 代码指南](https://alibaba.github.io/Alibaba-Java-Coding-Guidelines/)

### IDE 插件安装（可选）

* **Idea IDE**：[p3c-idea-plugin 安装](https://github.com/alibaba/p3c/blob/master/idea-plugin/README.md)
* **Eclipse IDE**：[p3c-eclipse-plugin 安装](https://github.com/alibaba/p3c/blob/master/eclipse-plugin/README.md)

总之，**任何帮助都是贡献**。