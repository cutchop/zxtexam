package net.whzxt.zxtexam;

public class NumberAction extends BaseAction {

	public NumberAction(Metadata md, int dataid, int times, int min, int max) {
		super(md, dataid, times, min, max);
	}

	@Override
	public Boolean CheckOK(int step) {
		if (Step != step) {
			return true;
		}
		if (_max > 0) {
			return true;
		}
		if (_isOK) {
			return true;
		}
		if (_md.getData(_dataid) >= _min) { // 大于最小值就通过
			_isOK = true;
		}
		return _isOK;
	}

	@Override
	public Boolean CheckError(int step) {
		if (Step != step) {
			return false;
		}
		if (_max == 0) {
			return false;
		}
		if (_isError) {
			return false;
		}
		if (_md.getData(_dataid) > _max || _md.getData(_dataid) < _min) {
			//大于最大值或者小于最小值就扣分
			_isError = true;
		}
		return _isError;
	}
}
