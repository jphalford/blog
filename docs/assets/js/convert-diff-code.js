hljs.initHighlightingOnLoad();

document.addEventListener('DOMContentLoaded', () => {
    // , outputFormat: 'side-by-side'
    const configuration = { drawFileList: false, matching: 'none', highlight: 'true' };
    const elements = document.getElementsByClassName('language-diff')
    console.log('Found Elements ' + elements);
    while (elements.length > 0) {
      const diffElement = elements[0];
      const diff2HtmlElement = document.createElement("div");
      const diff = diffElement.innerText;
      diffElement.parentNode.parentNode.replaceChild(diff2HtmlElement, diffElement.parentNode);
      const diff2htmlUi = new Diff2HtmlUI(diff2HtmlElement, diff, configuration);
      diff2htmlUi.draw();
    }
});