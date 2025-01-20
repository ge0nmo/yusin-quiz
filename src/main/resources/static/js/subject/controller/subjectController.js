import * as subjectModel from '../model/subjectModel.js'
import subjectResultView from '../view/subjectResultView.js'

const controlSubject = async function(){
    try{

        // 1) Load subject results
        await subjectModel.loadSubject();

        // 2) Render subject results
        subjectResultView.render(subjectModel.state.data);

        // 3) Render initial Pagination

    } catch(error){
        console.error(error);
    }

}

const controlPagination = function(goToPage) {

}

const init = function(){
    subjectResultView.addHandlerRender(controlSubject);
}

init();