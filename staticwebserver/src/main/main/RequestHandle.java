package main;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public interface RequestHandle {

  void process(ServletRequest sr1, ServletResponse sr2);

}
