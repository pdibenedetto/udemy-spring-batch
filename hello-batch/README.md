# Steps
* Two types - tasklet and chunks

## Tasklet
* appropriate for anything outside of item-based processing
* E.g. FTP a file, send an email

## Chunk
* processing items individually
* item based
* ItemReader
* ItemProcessor (optional) - applies additional logic, transformation to each item
* ItemWriter - provides the output of the step