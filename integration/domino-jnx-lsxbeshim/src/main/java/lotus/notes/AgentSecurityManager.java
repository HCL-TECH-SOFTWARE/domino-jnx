package lotus.notes;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

public class AgentSecurityManager extends SecurityManager {
  @Override
  public void checkAccept(String host, int port) {
    super.checkAccept(host, port);
  }
  
  @Override
  public void checkAccess(Thread t) {
    super.checkAccess(t);
  }
  
  @Override
  public void checkAccess(ThreadGroup g) {
    super.checkAccess(g);
  }
  
  public void checkAwtEventQueueAccess() {
  }
  
  @Override
  public void checkConnect(String host, int port) {
    super.checkConnect(host, port);
  }
  
  @Override
  public void checkConnect(String host, int port, Object context) {
    super.checkConnect(host, port, context);
  }
  
  @Override
  public void checkCreateClassLoader() {
    super.checkCreateClassLoader();
  }
  
  @Override
  public void checkDelete(String file) {
    super.checkDelete(file);
  }
  
  @Override
  public void checkExec(String cmd) {
    super.checkExec(cmd);
  }
  
  @Override
  public void checkExit(int status) {
    super.checkExit(status);
  }
  
  @Override
  public void checkLink(String lib) {
    super.checkLink(lib);
  }
  
  @Override
  public void checkListen(int port) {
    super.checkListen(port);
  }
  
  public void checkMemberAccess(Class<?> clazz, int which) {
  }
  
  @Override
  public void checkMulticast(InetAddress maddr) {
    super.checkMulticast(maddr);
  }
  
  public void checkMulticast(InetAddress maddr, byte ttl) {
  }
  
  @Override
  public void checkPackageAccess(String pkg) {
    super.checkPackageAccess(pkg);
  }
  
  @Override
  public void checkPackageDefinition(String pkg) {
    super.checkPackageDefinition(pkg);
  }
  
  @Override
  public void checkPermission(Permission perm) {
    super.checkPermission(perm);
  }
  
  @Override
  public void checkPrintJobAccess() {
    super.checkPrintJobAccess();
  }
  
  @Override
  public void checkPropertiesAccess() {
    super.checkPropertiesAccess();
  }
  
  @Override
  public void checkPropertyAccess(String key) {
    super.checkPropertyAccess(key);
  }
  
  @Override
  public void checkRead(FileDescriptor fd) {
    super.checkRead(fd);
  }
  
  @Override
  public void checkRead(String file) {
    super.checkRead(file);
  }
  
  @Override
  public void checkRead(String file, Object context) {
    super.checkRead(file, context);
  }
  
  @Override
  public void checkSecurityAccess(String target) {
    super.checkSecurityAccess(target);
  }
  
  @Override
  public void checkSetFactory() {
    super.checkSetFactory();
  }
  
  public void checkSystemClipboardAccess() {
  }
  
  public boolean checkTopLevelWindow(Object window) {
    return false;
  }
  
  @Override
  public void checkWrite(FileDescriptor fd) {
    super.checkWrite(fd);
  }
  
  @Override
  public void checkWrite(String file) {
    super.checkWrite(file);
  }
  
  public Object getExtenderSecurityContext() {
    return null;
  }
  
  @Override
  public Object getSecurityContext() {
    return super.getSecurityContext();
  }
  
  @Override
  public ThreadGroup getThreadGroup() {
    return super.getThreadGroup();
  }
  
  protected void newSecurityContext(AgentSecurityContext paramAgentSecurityContext, ThreadGroup paramThreadGroup) {
    
  }
  
  public void removeSecurityContext(ThreadGroup paramThreadGroup) {
    
  }
  
  public void setAgentSecurityManagerExtender(AgentSecurityManagerExtender paramAgentSecurityManagerExtender) {
    
  }
}
