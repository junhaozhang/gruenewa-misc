name := "gruenewa-wsclient"

organization := "gruenewa"

version := "1.0"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
   "com.sun.xml.wss" % "xws-security" % "3.0" 
)

ivyXML :=
  <dependencies>
    <exclude module="xmldsig"/>
  </dependencies>

