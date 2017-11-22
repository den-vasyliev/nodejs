import jenkins.Configuration
testScript = """true
    |RESULT=true
    |exit \$RESULT
    """.stripMargin()

job('ci_approve_qlmm') {
    lockableResources(Configuration.LOCKABLE_RESOURCE_QLMM_CORE)
    description('Test qlmm pull request. Warning! Manual job modifications would be overwritten by seed job.')
    wrappers {
        colorizeOutput()
        preBuildCleanup()
        timeout {
            absolute(15)
        }
    }
    triggers {
        pollSCM {
            scmpoll_spec('')
        }
    }
    scm {
        git {
            remote {
                refspec('+refs/pull-requests/*:refs/remotes/origin/pr/*')
                url("https://github.com/den-vasyliev/msrn.git")
                credentials(Configuration.GIT_CREDENTIALS)
            }
            branch('')
        }
    }
    steps {
        shell(testScript)
    }
    publishers() {
        bitbucketBuildStatusNotifier {
            baseUrl("")
            username("")
            password("")
        }
        junit {
            testResults('**/junit.xml')
            allowEmptyResults(true)
        }
        rcov {
            reportDirectory('coverage')
        }
    }
}
job('publish_qlmm') {
    lockableResources(Configuration.LOCKABLE_RESOURCE_DOCKER)
    description('Create archive with QLMM. Warning! Manual job modifications would be overwritten by seed job.')
    wrappers {
        colorizeOutput()
        preBuildCleanup()
        timeout {
            absolute(15)
        }
    }
    triggers {
        pollSCM {
            scmpoll_spec('')
        }
    }
    scm {
        git {
            remote {
                name('origin')
                url("https://github.com/den-vasyliev/msrn.git")
                credentials(Configuration.GIT_CREDENTIALS)
            }
            branch('master')
        }
    }
    steps {
        shell(testScript)
        shell(archive)
    }
    publishers {
        archiveArtifacts(qlmmTar)
        junit {
            testResults('**/junit.xml')
            allowEmptyResults(true)
        }
        downstream('ci_update_qsys_qlmm_dev', 'SUCCESS')
    }
    rcov {
        reportDirectory('coverage')
    }
}
job('ci_update_qlmm_ui') {
    description('Update QLMM ui version to the latest. Warning! Manual job modifications would be overwritten by seed job.')
    wrappers {
        colorizeOutput()
        preBuildCleanup()
        timeout {
            absolute(15)
        }
    }
    scm {
        git {
            remote {
                name('origin')
                url('https://github.com/den-vasyliev/msrn.git')
                credentials(Configuration.GIT_CREDENTIALS)
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
    description('Bitbucket';)
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
