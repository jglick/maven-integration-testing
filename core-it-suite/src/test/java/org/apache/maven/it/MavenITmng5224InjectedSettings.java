package org.apache.maven.it;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.it.util.ResourceExtractor;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.mortbay.jetty.Server;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a test set for <a href="http://jira.codehaus.org/browse/MNG-5224">MNG-5175</a>.
 * test correct injection of settings with profiles in ${settings} in mojo
 *
 * @version $Id$
 */
public class MavenITmng5224InjectedSettings
    extends AbstractMavenIntegrationTestCase
{
    private Server server;

    private int port;

    public MavenITmng5224InjectedSettings()
    {
        // olamy probably doesn't work with 3.x before 3.0.5
        super( "[2.0.3,3.0-alpha-1),[3.0.5,)" );
    }


    /**
     *
     */
    public void testmng5224_ReadSettings()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/mng-5224" );

        Verifier verifier = newVerifier( testDir.getAbsolutePath() );

        verifier.getCliOptions().add( "--settings" );
        verifier.getCliOptions().add( "settings.xml" );
        //verifier.
        verifier.executeGoal( "validate" );

        File settingsFile = new File( verifier.getBasedir(), "target/settings-dump.xml" );

        FileReader fr = new FileReader( settingsFile );

        Xpp3Dom dom = Xpp3DomBuilder.build( fr );

        Xpp3Dom profilesNode = dom.getChild( "profiles" );

        Xpp3Dom[] profileNodes = profilesNode.getChildren( "profile" );

        // 3 from the user settings + 1 for the global settings used for its
        assertEquals( 4, profileNodes.length );

        /**
         <profiles>
         <profile>
         <id>apache</id>
         <activation>
         <activeByDefault>true</activeByDefault>
         </activation>
         <properties>
         <run-its>true</run-its>
         </properties>
         </profile>
         <profile>
         <id>release</id>
         <properties>
         <gpg.passphrase>verycomplicatedpassphrase</gpg.passphrase>
         </properties>
         </profile>
         <profile>
         <id>fast</id>
         <properties>
         <maven.test.skip>true</maven.test.skip>
         <skipTests>true</skipTests>
         </properties>
         </profile>
         </profiles>
         **/

        List<String> profileIds = new ArrayList<String>( 4 );

        for ( Xpp3Dom node : profileNodes )
        {
            Xpp3Dom idNode = node.getChild( "id" );
            profileIds.add( idNode.getValue() );
            if ( "apache".equals( idNode.getName() ) )
            {
                Xpp3Dom propsNode = node.getChild( "properties" );
                assertEquals( "true", propsNode.getChild( "run-its" ).getValue() );
            }
            if ( "release".equals( idNode.getName() ) )
            {
                Xpp3Dom propsNode = node.getChild( "properties" );
                assertEquals( "verycomplicatedpassphrase", propsNode.getChild( "gpg.passphrase" ).getValue() );
            }
            if ( "fast".equals( idNode.getName() ) )
            {
                Xpp3Dom propsNode = node.getChild( "properties" );
                assertEquals( "true", propsNode.getChild( "maven.test.skip" ).getValue() );
                assertEquals( "true", propsNode.getChild( "skipTests" ).getValue() );
            }
        }

        assertTrue( profileIds.contains( "apache" ) );
        assertTrue( profileIds.contains( "release" ) );
        assertTrue( profileIds.contains( "fast" ) );
        assertTrue( profileIds.contains( "it-defaults" ) );

        /**
         <activeProfiles>
         <activeProfile>it-defaults</activeProfile>
         <activeProfile>apache</activeProfile>
         </activeProfiles>
         */

        Xpp3Dom activeProfilesNode = dom.getChild( "activeProfiles" );
        assertEquals( 2, activeProfilesNode.getChildCount() );

        List<String> activeProfiles = new ArrayList<String>( 2 );

        for ( Xpp3Dom node : activeProfilesNode.getChildren() )
        {
            activeProfiles.add( node.getValue() );
        }

        assertTrue( activeProfiles.contains( "apache" ) );
        assertTrue( activeProfiles.contains( "it-defaults" ) );

    }


}