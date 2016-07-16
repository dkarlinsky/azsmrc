<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:wfw="http://wellformedweb.org/CommentAPI/">
    <xsl:output method="html"/>
    <xsl:template match="/">
        <div><p>Show last: <a href="?render=html&amp;last=1h">Hour</a> - <a href="?render=html&amp;last=1d">Day</a> - <a href="?render=html&amp;last=7d">Week</a></p></div>
        <xsl:apply-templates select="/rss/channel"/>
    </xsl:template>
    <xsl:template match="/rss/channel">
        <h1><xsl:value-of select="title"/></h1>
        <p><xsl:value-of select="description"/></p>
        <ul>
            <xsl:apply-templates select="item"/>
        </ul>
    </xsl:template>
    <xsl:template match="/rss/channel/item">
        <li>
            <b><xsl:value-of select="title"/></b>
            <p><xsl:value-of select="description" disable-output-escaping="yes" /></p>
        </li>
    </xsl:template>
</xsl:stylesheet>