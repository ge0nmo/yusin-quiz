import View from "../../View.js";

class SubjectPaginationView extends View {
    _parentElement = document.querySelector('.paging');

    addHandlerRender(handler){

    }

    _generateMarkup(){
        const currentPage = this._data.page.currentPage;
        const totalPages = this._data.page.totalPages;
        let html = ``;
        // 첫 페이지
        if(currentPage === 0){
            html += `
                 <li class="page-item">
                    <a class="page-link" href="javascript:void(0);">이전 페이지</a>
                </li>
            `
        }

        const startPage = Math.max(0, currentPage - 2);
        const endPage = Math.min(totalPages - 1, currentPage + 2);

        for(let i = startPage; i <= endPage; i++){
            if(i === currentPage){
                html += `
                            <li class="page-item active">
                                <a class="page-link">${i + 1} <span class="sr-only">(current)</span> </a>
                            </li>
                        `;
            } else{
                html += `
                    <li class="page-item">
                        <a class="page-link" href="javascript:void(0);">${i + 1}</a>
                    </li>
                `;
            }
        }

        if(currentPage < totalPages - 1){
            html += `
                <li class="page-item">
                    <a class="page-link" href="javascript:void(0);">마지막 페이지</a>
                </li>
            `;
        }

        html += `</ul>`;

        return html;
    }
}