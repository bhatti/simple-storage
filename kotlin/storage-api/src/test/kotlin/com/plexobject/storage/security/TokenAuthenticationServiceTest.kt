package com.plexobject.storage.security


import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

class TokenAuthenticationServiceTest {
    private val response = mock<HttpServletResponse>(HttpServletResponse::class.java)
    private val request = mock<HttpServletRequest>(HttpServletRequest::class.java)
    private val session = mock<HttpSession>(HttpSession::class.java)
    private val attributes = mock<RequestAttributes>(RequestAttributes::class.java)
    private var service: TokenAuthenticationService? = null

    @Before
    fun setUp() {
        service = TokenAuthenticationService("secret", "HS512", 10)
        RequestContextHolder.setRequestAttributes(attributes, true)
        `when`<HttpSession>(request.getSession()).thenReturn(session)
    }

    @Test
    fun testAddAuthentication() {
        `when`<String>(request.getRemoteAddr()).thenReturn("remote")
        `when`<String>(request.getLocalAddr()).thenReturn("local")
        service!!.addAuthentication(response, "bob")
        verify<HttpServletResponse>(response).addHeader(ArgumentMatchers.eq(TokenAuthenticationService.HEADER_STRING), ArgumentMatchers.any(String::class.java))
        Assert.assertEquals("XXXX", "Bearer XXXX".replace(".*\\s".toRegex(), ""))
    }

    @Test
    @Throws(Exception::class)
    fun testGetAuthenticationNullToken() {
        `when`<String>(request.getRemoteAddr()).thenReturn("remote")
        `when`<String>(request.getLocalAddr()).thenReturn("local")
        Assert.assertNull(service!!.getAuthentication(request))
    }

    @Test
    @Throws(Exception::class)
    fun testGetAuthenticationParamsNullUser() {
        `when`<String>(request.getRemoteAddr()).thenReturn("remote")
        `when`<String>(request.getLocalAddr()).thenReturn("local")

        val claims = Jwts.claims()
        claims.put("u", "x")
        val token = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, service!!.secret).compact()
        `when`<String>(request.getParameter(TokenAuthenticationService.HEADER_STRING)).thenReturn(token)
        Assert.assertNull(service!!.getAuthentication(request))
    }

    @Test
    @Throws(Exception::class)
    fun testGetAuthenticationNullUser() {
        `when`<String>(request.getRemoteAddr()).thenReturn("remote")
        `when`<String>(request.getLocalAddr()).thenReturn("local")

        val claims = Jwts.claims()
        claims.put("u", "x")
        val token = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, service!!.secret).compact()
        `when`<String>(request.getHeader(TokenAuthenticationService.HEADER_STRING)).thenReturn("Bearer $token")
        Assert.assertNull(service!!.getAuthentication(request))
    }

    @Test
    @Throws(Exception::class)
    fun testGetAuthenticationBadToken() {
        `when`<String>(request.getRemoteAddr()).thenReturn("remote")
        `when`<String>(request.getLocalAddr()).thenReturn("local")

        `when`<String>(request.getHeader(TokenAuthenticationService.HEADER_STRING)).thenReturn("Bearer $BAD_TOKEN")
        Assert.assertNull(service!!.getAuthentication(request))
    }

    @Test
    @Throws(Exception::class)
    fun testGetAuthenticationGoodToken() {
        `when`<String>(request.getRemoteAddr()).thenReturn("remote")
        `when`<String>(request.getLocalAddr()).thenReturn("local")

        val token = service!!.generateToken("user")
        `when`<String>(request.getHeader(TokenAuthenticationService.HEADER_STRING)).thenReturn("Bearer $token")
        service!!.getAuthentication(request)
    }

    @Test
    @Throws(Exception::class)
    fun testGetAuthenticationBasicToken() {
        `when`<String>(request.getRemoteAddr()).thenReturn("remote")
        `when`<String>(request.getLocalAddr()).thenReturn("local")

        var token = service!!.generateToken("appname") + ":"
        token = Base64.getEncoder().encodeToString(token.toByteArray(charset("utf-8")))
        `when`<String>(request.getHeader(TokenAuthenticationService.HEADER_STRING)).thenReturn("Basic $token")
        service!!.getAuthentication(request)
    }

    //@Test
    @Throws(Exception::class)
    fun testGetAuthenticationWithDashboardAuth() {
        `when`<Any>(session.getAttribute("authenticated")).thenReturn(true)
        service!!.getAuthentication(request)
        verify<HttpServletRequest>(request, times(0)).getHeader(TokenAuthenticationService.HEADER_STRING)
    }

    @Test
    @Throws(Exception::class)
    fun testGetAuthenticationWithLocalAddress() {
        `when`<String>(request.getRemoteHost()).thenReturn("0:0:0:0:0:0:0:1")
        service!!.getAuthentication(request)
        verify<HttpServletRequest>(request, times(0)).getHeader(TokenAuthenticationService.HEADER_STRING)
    }

    @Test
    @Throws(Exception::class)
    fun testGetAuthenticationWithLoopAddress() {
        `when`<String>(request.getRemoteHost()).thenReturn("127.0.0.1")
        service!!.getAuthentication(request)
        verify<HttpServletRequest>(request, times(0)).getHeader(TokenAuthenticationService.HEADER_STRING)
    }

    companion object {
        private val BAD_TOKEN = "XXXX"
    }
}
