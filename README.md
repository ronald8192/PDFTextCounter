# PDF Text Counter #
---
PDF word count, output to `summary.csv`

## Options ##
* `i`: input directory (you PDF should put in here)
* `o`: output directory
* `s`: start read from PDF page (for skip covers) (first page is 1)

## Example ##
* Put PDF files in `pdfTextCounter/pdfs/`
* output to `pdfTextCounter/txt/`
* Start reading every PDF from page 2

`java -jar PDFTextCounter.jar -i pdfTextCounter/pdfs/ -o pdfTextCounter/txt/ -s 2`
