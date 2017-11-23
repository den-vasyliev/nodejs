manager.listener.logger.println("commit notification hd387ry34oiuhr3ofin")
manager.listener.logger.println("* [new tag] v0.13.6 -> v.0.13.6")


pattern_commit = ~/commit notification(.*)/
pattern_version = ~/\[new tag\](.*)/
manager.build.logFile.eachLine { line ->  matcher_comit = pattern_commit.matcher(line)
manager.build.logFile.eachLine { line ->  matcher_version = pattern_version.matcher(line)
    if(matcher_commit.matches()) {
        commit = matcher.group(1)
    }
    if(matcher_version.matches()) {
        version = matcher.group(1)
    }

}
manager.addShortText("<a href=https://github.com/den-vasyliev/nodejs/commit/$commit target=_blank>$version</a>")
manager.listener.logger.println("I want to see this line in my job's output: $commit")
manager.listener.logger.println("I want to see this line in my job's output: $version")
manager.addBadge("star-gold.gif", "icon from greenballs plugin")
manager.addShortText("<a href=https://github.com/den-vasyliev/nodejs/commit/$commit target=_blank>commit</a>", "grey", "white", "0px", "white")