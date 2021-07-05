# auto-cluster-maven-plugin

auto-cluster-maven-plugin is a Maven plugin that extracts Java dependency
information, runs a hierarchical clustering algorithm, and then organizes your
source files accordingly.

## Usage

To run this plugin,

```bash
mvn io.github.patrickdoc:auto-cluster-maven-plugin:cluster
```

This will generate a folder `src/main/auto-cluster-maven-plugin1234...`
containing the new structure for review.

If you use version control and would like to use this structure permanently, you
can disable the `dryRun` option to overwrite your source files.

> :warning: **WARNING**: This will delete your existing files. Please be very careful.

```bash
mvn io.github.patrickdoc:auto-cluster-maven-plugin:cluster -DdryRun=false
```

## Why?

I think dependencies are an under-examined aspect of code and we can do a lot
more with them.

This plugin has two goals.

First, for individual projects, the goal is to make
the internal dependency structure easy to analyze. By putting it front and
center, you will hopefully be able to identify and resolve potential structural
issues in your code.

Second, on a larger scale, the goal is to provide a language for talking about
code organization and style.

I don't like organizing interfaces into an `inf` package or enums into an `enum`
package, but there is no productive conversation we can have about that.

On the other hand, if you submit a pull request to increase the effect of
transitive dependencies on the clustering algorithm, then we can look at
concrete examples of how it would work on any codebase. This seems like a much
better starting point than "I don't like X".

For a longer form dev log and discussion, see
[here](https://patrickdoc.github.io/dependencies.html).

## Example

The ClassGraph repo is as good an example of any of medium sized project with non-zero complexity in the code. So I've used it as an example here. Note, this is not a criticism of the existing structure. In fact, I'm quite pleased that the plugin reproduces some of the existing structure.

Running a dry run in the ClassGraph repo, you can see a side by side comparison of the original repo:
<img alt="Original ClassGraph source" src="https://github.com/patrickdoc/auto-cluster-maven-plugin/blob/master/imgs/classgraph.png">

and the clustered code:
<img alt="Clustered ClassGraph source" src="https://github.com/patrickdoc/auto-cluster-maven-plugin/blob/master/imgs/classgraph-cluster.png">

You can also browse the files in [this fork](https://github.com/patrickdoc/classgraph/tree/clustered/src/main).

## Acknowledgements and References

[ClassGraph](https://github.com/classgraph/classgraph): This project powers the
dependency data extraction, but can also do much more

[Hierarchical Clustering Primer](https://uc-r.github.io/hc_clustering): A useful
introduction to hierarchical clustering

[Clustering Algorithm](https://arxiv.org/pdf/1109.2378.pdf): The base algorithm
for clustering, available in Python and R packages as `fastcluster`
