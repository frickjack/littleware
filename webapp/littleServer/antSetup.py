import os

fh = open( "webstart.xml", 'w' )
fh.write( """<project default="-littlelib-process" basedir=\".\">
<target name="-littlelib-process" depends="-init-project">
    
       <!--
    <antcall target="${param.target}">
        <param name="param.jarFile" value="scratch.jar" />
        <param name="param.jarDir"  value="../../../scratch/dist" />
    </antcall>
    -->
"""
)

for scan in os.listdir( "lib/client" ):
	if scan.endswith( ".jar" ):
	    fh.write( """    <antcall target="${param.target}">
        <param name="param.jarFile" value="%s" />
        <param name="param.jarDir"  value="lib/client" />
        </antcall>
              """ % (scan)
              )
         
fh.write ( """
</target>
</project>
"""
)
fh.close()
