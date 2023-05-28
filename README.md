# GOST-Block-Cipher-Implementation-Java
GOST Block Cipher implementation algorithm with java programming language

# Requirements
Java 10/11

# How to Run
```
String msg = "I Love Code";
Gost gost = new Gost(null);
String key = gost.generateStringKey();
byte[] encMsg = gost.encrypt(msg,key);
String msgData = gost.decrypt(encMsg,key);
System.out.println(msgData);
```
