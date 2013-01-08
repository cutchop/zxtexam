package net.whzxt.zxtexam;

public abstract class BaseAction {
	public String Itemname = "未知项目";
	public String Err = "未知原因";
	public int Fenshu = 100;
	public int Step = 1;
	protected Metadata _md;
	protected int _dataid = 0;
	protected int _times = 0;
	protected int _min = 0;
	protected int _max = 0;
	protected Boolean _isOK = false;
	protected Boolean _isError = false;

	public BaseAction(Metadata md, int dataid, int times, int min, int max) {
		this._md = md;
		this._dataid = dataid;
		this._times = times;
		this._max = max;
		this._min = min;
	}
	
	
	/*
	 * 是否必须要通过
	 */
	public Boolean IsMustOK(int step) {
		if (step != Step) {
			return false;
		}
		if (_dataid < 20) {
			if (Math.abs(_times) > 1) {
				return true;
			}
		} else {
			if (Math.abs(_min) > 0 && Math.abs(_max) == 0) {
				return true;
			}
		}
		return false;
	}

	/*
	 * 是否需要等待超时
	 */
	public Boolean IsWaitTimeout(int step) {
		if (step != Step) {
			return false;
		}
		if (_dataid < 20) {
			if (Math.abs(_times) == 1) {
				return true;
			}
		} else {
			if (Math.abs(_max) > 0) {
				return true;
			}
		}
		return false;
	}

	/*
	 * 当前步骤是否已通过
	 */
	public Boolean IsStepOK(int step) {
		if (Step != step) {
			return true;
		}
		if (_dataid < 20) {
			if (Math.abs(_times) == 1) {
				return true;
			}
		} else {
			if (Math.abs(_max) > 0) {
				return true;
			}
		}
		return _isOK;
	}

	/*
	 * 计算是否满足条件
	 */
	public abstract Boolean CheckOK(int step);

	/*
	 * 计算是否需要扣分
	 */
	public abstract Boolean CheckError(int step);
}
