package main.middle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class RequestFacade implements ServletRequest {

  private ServletRequest servletRequest;

  public RequestFacade(ServletRequest servletRequest) {
    this.servletRequest = servletRequest;
  }

  @Override
  public Object getAttribute(String s) {
    return servletRequest.getAttribute(s);
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return servletRequest.getAttributeNames();
  }

  @Override
  public String getCharacterEncoding() {
    return servletRequest.getCharacterEncoding();
  }

  @Override
  public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
    servletRequest.setCharacterEncoding(s);
  }

  @Override
  public int getContentLength() {
    return servletRequest.getContentLength();
  }

  @Override
  public long getContentLengthLong() {
    return servletRequest.getContentLengthLong();
  }

  @Override
  public String getContentType() {
    return servletRequest.getContentType();
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return servletRequest.getInputStream();
  }

  @Override
  public String getParameter(String s) {
    return servletRequest.getParameter(s);
  }

  @Override
  public Enumeration<String> getParameterNames() {
    return servletRequest.getParameterNames();
  }

  @Override
  public String[] getParameterValues(String s) {
    return servletRequest.getParameterValues(s);
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    return servletRequest.getParameterMap();
  }

  @Override
  public String getProtocol() {
    return servletRequest.getProtocol();
  }

  @Override
  public String getScheme() {
    return servletRequest.getScheme();
  }

  @Override
  public String getServerName() {
    return servletRequest.getServerName();
  }

  @Override
  public int getServerPort() {
    return servletRequest.getServerPort();
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return servletRequest.getReader();
  }

  @Override
  public String getRemoteAddr() {
    return servletRequest.getRemoteAddr();
  }

  @Override
  public String getRemoteHost() {
    return servletRequest.getRemoteHost();
  }

  @Override
  public void setAttribute(String s, Object o) {
    servletRequest.setAttribute(s, o);
  }

  @Override
  public void removeAttribute(String s) {
    servletRequest.removeAttribute(s);
  }

  @Override
  public Locale getLocale() {
    return servletRequest.getLocale();
  }

  @Override
  public Enumeration<Locale> getLocales() {
    return servletRequest.getLocales();
  }

  @Override
  public boolean isSecure() {
    return servletRequest.isSecure();
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String s) {
    return servletRequest.getRequestDispatcher(s);
  }

  @Override
  public String getRealPath(String s) {
    return servletRequest.getRealPath(s);
  }

  @Override
  public int getRemotePort() {
    return servletRequest.getRemotePort();
  }

  @Override
  public String getLocalName() {
    return servletRequest.getLocalName();
  }

  @Override
  public String getLocalAddr() {
    return servletRequest.getLocalAddr();
  }

  @Override
  public int getLocalPort() {
    return servletRequest.getLocalPort();
  }

  @Override
  public ServletContext getServletContext() {
    return servletRequest.getServletContext();
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException {
    return servletRequest.startAsync();
  }

  @Override
  public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
      throws IllegalStateException {
    return servletRequest.startAsync();
  }

  @Override
  public boolean isAsyncStarted() {
    return servletRequest.isAsyncStarted();
  }

  @Override
  public boolean isAsyncSupported() {
    return servletRequest.isAsyncSupported();
  }

  @Override
  public AsyncContext getAsyncContext() {
    return servletRequest.getAsyncContext();
  }

  @Override
  public DispatcherType getDispatcherType() {
    return servletRequest.getDispatcherType();
  }
}
