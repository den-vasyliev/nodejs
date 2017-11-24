version="1.13.9"
testScript = """
    |RESULT=0
    |exit \$RESULT
    """.stripMargin()


job('ci_approve_qlmm') {
    description(' qlmm pull request. Warning! Manual job modifications would be overwritten by seed job.')
    triggers {
        pollSCM {
            scmpoll_spec('')
        }
        scm '* * * * * '
    }
    scm {
        git {
            remote {
                refspec('+refs/pull-requests/*:refs/remotes/origin/pr/*')
                url("https://github.com/den-vasyliev/msrn-cook.git")
                
            }
            branch('')
        }
    }
    steps {
        shell(testScript)
    }
    publishers() {
        
        junit {
            testResults('**/junit.xml')
            allowEmptyResults(true)
        }
        downstream('publish_qlmm', 'SUCCESS')
    }
}
job('publish_qlmm') {
   
    description('Create archive with QLMM. Warning! Manual job modifications would be overwritten by seed job.')
    
    triggers {
        pollSCM {
            scmpoll_spec('')
        }
    }
    scm {
        git {
            remote {
                name('origin')
                url("https://github.com/den-vasyliev/msrn-cook.git")
               
            }
            branch('master')
        }
    }
    steps {
        shell(testScript)

    }

    publishers {
     //   buildDescription('','<a href="" target=_blank>test</a>')

        groovyPostBuild '''
manager.listener.logger.println("commit notification a864681cd8b4f9ac5a9d4bf91a33bee253ebbc84")
manager.listener.logger.println("* [new tag] v0.13.6 -> v.0.13.6")

manager.build.logFile.eachLine { 
   line -> l=line
 
 try {commit=(l =~ /commit notification (.*)/)[0][1]} catch(Exception ex) {;}
 try {version=(l =~ /[new tag].*->(.*)/)[0][1]} catch(Exception ex) {;}
}
manager.addShortText("<a href=https://github.com/den-vasyliev/msrn-cook/commit/$commit target=_blank>$version</a>") 
        '''.stripIndent().trim()

        junit {
            testResults('**/junit.xml')
            allowEmptyResults(true)
        }
        downstream('ci_update_qsys_qlmm_dev', 'SUCCESS')
    }
   
}
job('ci_update_qlmm_ui') {
    description('Update QLMM ui version to the latest. Warning! Manual job modifications would be overwritten by seed job.')
    
    scm {
        git {
            remote {
                name('origin')
                url('https://github.com/den-vasyliev/msrn-cook.git')
                
            }
            branch('master')
        }
    }
    steps {
        shell(
            '''echo OK'''.stripMargin()
        )
    }
    publishers() {
        downstream('publish_qlmm', 'SUCCESS')
    }
}
listView('QLMM') {
    description('Bitbucket')
    filterBuildQueue()
    filterExecutors()
    jobs {
        name('ci_approve_qlmm')
        name('publish_qlmm')
        name('ci_update_qlmm_ui')
        name('ci_update_qsys_qlmm_dev')
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}
