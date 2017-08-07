BRANCH="master"

# Are we on the right branch?
if [ "$TRAVIS_BRANCH" = "$BRANCH" ]; then

  # Is this not a Pull Request?
  if [ "$TRAVIS_PULL_REQUEST" = false ]; then

    # Is this not a build which was triggered by setting a new tag?
    if [ -z "$TRAVIS_TAG" ]; then
      echo "Starting to tag commit.\n"

      git config --global user.email "travis@travis-ci.org"
      git config --global user.name "Travis"

      # Add tag and push to master.
      . ./app/version.properties
      git tag -a "v$VERSION_MAJOR.$VERSION_MINOR.$VERSION_PATCH-$VERSION_BUILD" -m "Travis build $TRAVIS_BUILD_NUMBER pushed a tag."
      git push origin --tags
      git fetch origin

      echo "Done magic with tags.\n"
  fi
  fi
fi