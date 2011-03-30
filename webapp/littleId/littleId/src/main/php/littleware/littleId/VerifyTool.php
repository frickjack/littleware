<?php

/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
namespace littleware\littleId;

require_once( 'HTTP/Request.php' );
require_once( 'Log.php' );


# Set log level once
$log = &\Log::singleton( 'display', '', 'littleware.littleId.VerifyTool' );
$log->setMask( \Log::MAX( \PEAR_LOG_WARNING ) ); 

/**
 * Simple tool to verify a set of littleId credentials
 */
class VerifyTool {
    //private $sBaseUrl = "http://eit.ebscohost.com/Services/SearchService.asmx/Search?prof=s4594776.main.eitwseds&pwd=ebs3742&startrec=1&numrec=10&query=";
    public $sBaseUrl = "http://beta.frickjack.com:8080/services/openId/services/verify/";

    /**
     * Return true if the given credentials verify with the given secret
     *
     * @param sSecret one-time token supplied by the littleId server
     * @param hCreds credentials associative array
     * @return true if credentials verify, false otherwise
     */
    function verify( $sSecret, $hCreds ) {
        $log = &\Log::singleton( 'display', '', 'littleware.littleId.VerifyTool' );
        $url = $this->sBaseUrl;
        $log->debug( "URL: $url" );
        try {
            $http = new \HTTP_Request( $url, array( "method" => "POST" ) );
            $http->addPostData( "secret", $sSecret );
            foreach( $hCreds as $key => $value ) {
                $http->addPostData( $key, $vaue );
            }
            $http->sendRequest();
            $sXml = $http->getResponseBody();
            if ( null == $sXml ) {
                return false;
            }
            // parse the result
            $log->debug( "Parsing: " . $sXml );
            return ( false !== \strpos( $sXml, "<verify>true</verify>" ) );
        } catch ( Exception $ex ) {
            $log->log( "Failed to connect to littleId server or to parse XML response".$ex->getMessage(), PEAR_LOG_ERR );
            return false;
        }
    }
}


require_once ( 'PHPUnit/Framework.php' );
 
/**
 * Test case.
 * Run with 'phpunit VerifyToolTester VerifyTool.php'
 */
class VerifyToolTester extends \PHPUnit_Framework_TestCase
{
    public function testVerifyTool()
    {
        $tool = new VerifyTool();
        $this->assertTrue( ! $tool->verify( "bogus", array( "email" => "email@email" ) ), 
             "Bogus credentials should fail verification"
             );
    } 
}

// Test runner ...
if( false ) {
    $log->setMask( \Log::MAX( \PEAR_LOG_DEBUG ) );
    $test = new VerifyToolTester();
    $test->testVerifyTool();
}

?>
