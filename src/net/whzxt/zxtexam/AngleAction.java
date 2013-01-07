package net.whzxt.zxtexam;

public class AngleAction extends BaseAction {
	private int _startAngle = -1;// 初始角度

	public AngleAction(Metadata md, int dataid, int times, int min, int max) {
		super(md, dataid, times, min, max);
	}

	@Override
	public Boolean CheckOK(int step) {
		if (Step != step) {
			return true;
		}
		if (_max != 0) {
			return true;
		}
		if (_isOK) {
			return true;
		}
		if (_min == 0) {
			return true;
		}
		if (_startAngle == -1) {
			_startAngle = _md.getData(_dataid);
		} else {
			int angle = _md.getData(_dataid) - _startAngle;
			if (angle > 200) {
				angle = (360 - angle) * -1;
			}
			if (_min > 0) {
				if (angle >= _min) {
					_isOK = true;
				}
			} else {
				if (angle <= _min) {
					_isOK = true;
				}
			}
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
		if (_startAngle == -1) {
			_startAngle = _md.getData(_dataid);
		} else {
			int angle = _md.getData(_dataid) - _startAngle;
			if (angle > 200) {
				angle = (360 - angle) * -1;
			}
			if (_max > 0) {
				if (angle > _max) {
					_isError = true;
				}
				if (_min < 0) {
					if (angle < _min) {
						_isError = true;
					}
				}
			} else {
				if (angle < _max) {
					_isError = true;
				}
			}
		}
		return _isError;
	}
}
