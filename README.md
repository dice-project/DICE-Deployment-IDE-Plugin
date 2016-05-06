# DICE Deployment IDE Plugin

Repo with Eclipse plugin that eases communication with DICE Deployment
Service.


## Maintainer info

Maintaining this repo is a bit tricky (we have a long-living gh-pages branch
that serves as a eclipse update site), so make sure you read next few
paragraphs carefully.

Master branch contains:

 1. sources of our plugin (`org.dice.deployments`),
 2. feature that wraps plugin (`org.dice.features.deployments`)
 3. and a site project for generating update site contents.

All of the projects should be maintained using Eclipse Mars.


### Setting up development environment

First, make sure you have eclipse installed and that plug-in development
plugins are installed (a bit of inception here;). Now simply start eclipse and
import projects from this repo.

Additionally, this plugin also requires `org.dice.ui` plugin, so we need to
import that too. You can find this plugin in [official DICE repo][dice-repo].

[dice-repo]: https://github.com/dice-project/DICE-Platform


### Testing plugin

Simply navigate to `org.dice.deployments` -> `plugin.xml` file in package
explorer and open it. On the right side of the *Overview* tab should be a
button and a link named *Launch an Eclipse application* that will start new
eclipse instance with our plugin loaded.


### Generating update site

Before trying to do anything, we will checkout `gh-pages` branch into a
separate folder using `git worktree` command. Exact command for this is

    git worktree add gh-pages gh-pages

This command will create new folder `gh-pages`. If we move inside this folder,
we will be automatically working on `gh-pages` branch. Sweet. Note that this
step only needs to be taken first time we setup the local repo folder. For all
other updates, we will reuse existing work tree.

To generate update site contents, navigate to `size` -> `site.xml` file and
open it. Now press *Build All* button and wait for the build process to
finish. This process should create `artifacs.jar` and `content.jar` archives
along with `features` and `plugins` folder.

Now we need to move all this to `gh-pages` branch. And thanks to out work tree
setup, this all actually amounts to simple `mv` command. The provided
`update-site.sh` script will do the moving for us. So we simply execute

    ./update-site.sh

Now we need `cd` into gh-pages folder and commit changes. And this is
basically all there is to it.


### Removing gh-pages folder

In order to clear `gh-pages` folder, we must first remove it from disk and
instruct git to prune work trees. Command are

    rm -rf gh-pages
    git worktree prune

This will remove folder and clear git's cache.
