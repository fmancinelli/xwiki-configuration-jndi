# Introduction

This module enables XWiki configuration override via JNDI. By using it, it will be possible to override any property in `xwiki.cfg` or `xwiki.properties` via JNDI.

# Installation

Just drop the JAR into the `WEB-INF/lib` directory of your XWiki installation.

# Configuration

By default overridden properties are looked in the `java:comp/env/xwiki/config` context. If you want to use another context, you can specify it using the `xwikiJNDIConfigContext` environment variable.

