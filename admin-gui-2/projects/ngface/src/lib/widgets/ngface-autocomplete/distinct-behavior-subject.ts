import {BehaviorSubject} from 'rxjs';

export class DistinctBehaviorSubject<T> extends BehaviorSubject<T>
{
  constructor(
    initialValue: T,
    private readonly equals: (a: T, b: T) => boolean = Object.is
  )
  {
    super(initialValue);
  }


  override next(value: T): void
  {
    //console.log('next', this.getValue(), value);
    if (!this.equals(this.getValue(), value))
    {
      super.next(value);
    }
  }
}
