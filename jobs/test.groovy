testScript = """ true
    |RESULT=0
    |echo * [new tag]
    |exit \$RESULT
    """.stripMargin()


job('ci_approve_qlmm') {
    description(' qlmm pull request. Warning! Manual job modifications would be overwritten by seed job.')
    label ('Linux')
    triggers {
        pollSCM {
            scmpoll_spec('')
        }
    }
    scm {
        git {
            remote {
                refspec('+refs/pull-requests/*:refs/remotes/origin/pr/*')
                url("https://github.com/den-vasyliev/nodejs.git")
                
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
                url("https://github.com/den-vasyliev/nodejs.git")
               
            }
            branch('master')
        }
    }
    steps {
        shell(testScript)

    }

postBuildPublishers {
        buildDescription(/.*new tag\] [^\s]* ([^\s]*)/)
    }


    publishers {
        
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
                url('https://github.com/den-vasyliev/nodejs.git')
                
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
